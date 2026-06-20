package org.ratelog.user.edit

import arrow.core.getOrElse
import org.ratelog.Email
import org.ratelog.ParseError
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.AppUserDetails
import org.ratelog.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class EditUserController(
    private val handler: EditUserHandler,
) {

    @GetMapping("/edit-profile")
    fun editProfilePage(): String = "edit-profile"

    @PostMapping("/edit-profile")
    fun editProfile(
        @CurrentUser user: User,
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("currentPassword") currentPassword: String,
        @RequestParam("newPassword", required = false) newPassword: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        val parsedUsername = Username.parse(username).getOrElse {
            redirectAttributes.addFlashAttribute("error", "edit.error.invalid.username.format")
            return "redirect:/profile"
        }

        val parsedEmail = Email.parse(email).getOrElse {
            redirectAttributes.addFlashAttribute("error", "edit.error.invalid.email.format")
            return "redirect:/profile"
        }

        val parsedCurrentPassword = Password.parse(currentPassword).getOrElse {
            redirectAttributes.addFlashAttribute("error", "edit.error.invalid.current.password.format")
            return "redirect:/profile"
        }

        val parsedNewPassword = newPassword?.ifEmpty { null }?.let {
            Password.parse(it).getOrElse {
                redirectAttributes.addFlashAttribute("error", "edit.error.invalid.new.password.format")
                return "redirect:/profile"
            }
        }

        return EditUserCommand(
            userId = user.id!!,
            username = parsedUsername,
            email = parsedEmail,
            currentPassword = parsedCurrentPassword,
            newPassword = parsedNewPassword,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/profile"
                },
                {
                    refreshUserDetails(username, email, newPassword)
                    "redirect:/profile"
                }
            )
    }

    private fun refreshUserDetails(username: String, email: String, password: String?) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentDetails = auth.principal as? AppUserDetails ?: return

        val newAuth = UsernamePasswordAuthenticationToken(
            AppUserDetails(
                id = currentDetails.id,
                username = username,
                email = email,
                password = password ?: currentDetails.password,
                lang = currentDetails.lang,
            ),
            auth.credentials,
            auth.authorities
        )
        SecurityContextHolder.getContext().authentication = newAuth
    }

    private fun mapError(error: EditUserHandlerError): String = when (error) {
        EditUserHandlerError.UserNotFound -> "edit.error.user.not.found"
        EditUserHandlerError.UsernameAlreadyExists -> "edit.error.username.already.exists"
        EditUserHandlerError.EmailAlreadyExists -> "edit.error.email.already.exists"
        EditUserHandlerError.InvalidCurrentPassword -> "edit.error.invalid.current.password"
    }
}
