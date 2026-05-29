package org.ratelog.user.edit

import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.AppUserDetails
import org.ratelog.user.User
import org.springframework.context.MessageSource
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Locale

@Controller
class EditUserController(
    private val handler: EditUserHandler,
    private val messageSource: MessageSource,
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
        redirectAttributes: RedirectAttributes,
        locale: Locale
    ): String =
        EditUserCommand(
            userId = user.id!!,
            username = Username(username),
            email = Email(email),
            currentPassword = Password(currentPassword),
            newPassword = newPassword?.ifEmpty { null }?.let { Password(it) },
        ).let(handler::handle)
            .mapLeft { mapError(it, locale) }
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

    private fun mapError(error: EditUserHandlerError, locale: Locale): String = when (error) {
        EditUserHandlerError.UserNotFound -> messageSource.getMessage("edit.error.user.not.found", null, locale)
        EditUserHandlerError.EmptyFields -> messageSource.getMessage("edit.error.empty.fields", null, locale)
        EditUserHandlerError.InvalidUsernameLength -> messageSource.getMessage("edit.error.invalid.username.length", null, locale)
        EditUserHandlerError.InvalidPasswordLength -> messageSource.getMessage("edit.error.invalid.password.length", null, locale)
        EditUserHandlerError.UsernameAlreadyExists -> messageSource.getMessage("edit.error.username.already.exists", null, locale)
        EditUserHandlerError.EmailAlreadyExists -> messageSource.getMessage("edit.error.email.already.exists", null, locale)
        EditUserHandlerError.InvalidCurrentPassword -> messageSource.getMessage("edit.error.invalid.current.password", null, locale)
    }
}
