package org.ratelog.user.edit

import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
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
        @RequestParam("lang") lang: String,
        redirectAttributes: RedirectAttributes
    ): String =
        EditUserCommand(
            userId = user.id!!,
            username = Username(username),
            email = Email(email),
            currentPassword = Password(currentPassword),
            newPassword = newPassword?.let { Password(it) },
            lang = Lang(lang),
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/edit-profile"
                },
                { "redirect:/profile" }
            )

    private fun mapError(error: EditUserHandlerError): String = when (error) {
        EditUserHandlerError.UserNotFound -> "User not found."
        EditUserHandlerError.EmptyFields -> "Username and email are required."
        EditUserHandlerError.InvalidUsernameLength -> "Username must be between 3 and 50 characters."
        EditUserHandlerError.InvalidPasswordLength -> "New password must be at least 8 characters."
        EditUserHandlerError.UsernameAlreadyExists -> "Username already exists."
        EditUserHandlerError.EmailAlreadyExists -> "Email already exists."
        EditUserHandlerError.InvalidCurrentPassword -> "Current password is incorrect."
    }
}
