package org.ratelog.user.register

import arrow.core.getOrElse
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Username
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import jakarta.servlet.http.HttpServletRequest
import org.ratelog.user.UserDetailsService

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
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest
    ): String {
        val browserLang = UserDetailsService.resolve(request)

        val parsedUsername = Username.parse(username).getOrElse {
            redirectAttributes.addFlashAttribute("error", "register.error.invalid.username.format")
            return "redirect:/register"
        }

        val parsedEmail = Email.parse(email).getOrElse {
            redirectAttributes.addFlashAttribute("error", "register.error.invalid.email.format")
            return "redirect:/register"
        }

        val parsedPassword = Password.parse(password).getOrElse {
            redirectAttributes.addFlashAttribute("error", "register.error.invalid.password.format")
            return "redirect:/register"
        }

        return RegisterUser(
            username = parsedUsername,
            email = parsedEmail,
            password = parsedPassword,
            lang = browserLang,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/register"
                },
                { "redirect:/login" }
            )
    }

    private fun mapError(error: RegisterHandlerError): String = when (error) {
        RegisterHandlerError.UsernameAlreadyExists -> "register.error.username.already.exists"
        RegisterHandlerError.EmailAlreadyExists -> "register.error.email.already.exists"
    }
}
