package org.raterr.user.register

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class RegisterController(
    private val handler: RegisterHandler,
) {

    @GetMapping("/register")
    fun registerPage(): String = "register"

    @PostMapping("/register")
    fun register(
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        redirectAttributes: RedirectAttributes
    ): String =
        RegisterUser(
            username = username,
            email = email,
            password = password,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/register"
                },
                { "redirect:/login" }
            )

    private fun mapError(error: RegisterHandlerError): String = when (error) {
        RegisterHandlerError.EmptyFields -> "All fields are required."
        RegisterHandlerError.InvalidUsernameLength -> "Username must be between 3 and 50 characters."
        RegisterHandlerError.InvalidPasswordLength -> "Password must be at least 8 characters."
        RegisterHandlerError.UsernameAlreadyExists -> "Username already exists."
        RegisterHandlerError.EmailAlreadyExists -> "Email already exists."
    }
}
