package org.ratelog.user.edit

import org.ratelog.Email
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
    ): String =
        EditUserCommand(
            userId = user.id!!,
            username = Username(username),
            email = Email(email),
            currentPassword = Password(currentPassword),
            newPassword = newPassword?.ifEmpty { null }?.let { Password(it) },
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    redirectAttributes.addFlashAttribute("username", username)
                    redirectAttributes.addFlashAttribute("email", email)
                    "redirect:/profile"
                },
                {
                    refreshUserDetails(username, email, newPassword)
                    "redirect:/profile"
                }
            )

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
        EditUserHandlerError.EmptyFields -> "edit.error.empty.fields"
        EditUserHandlerError.InvalidUsernameLength -> "edit.error.invalid.username.length"
        EditUserHandlerError.InvalidPasswordLength -> "edit.error.invalid.password.length"
        EditUserHandlerError.UsernameAlreadyExists -> "edit.error.username.already.exists"
        EditUserHandlerError.EmailAlreadyExists -> "edit.error.email.already.exists"
        EditUserHandlerError.InvalidCurrentPassword -> "edit.error.invalid.current.password"
    }
}
