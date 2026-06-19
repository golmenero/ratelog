package org.ratelog.user.register

import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Username
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import jakarta.servlet.http.HttpServletRequest
import org.ratelog.user.BrowserLangResolver

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
        val browserLang = BrowserLangResolver.resolve(request)

        return RegisterUser(
            username = username.let(::Username),
            email = email.let(::Email),
            password = password.let(::Password),
            lang = Lang.valueOf(browserLang.language),
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
        RegisterHandlerError.EmptyFields -> "register.error.empty.fields"
        RegisterHandlerError.InvalidUsernameLength -> "register.error.invalid.username.length"
        RegisterHandlerError.InvalidPasswordLength -> "register.error.invalid.password.length"
        RegisterHandlerError.UsernameAlreadyExists -> "register.error.username.already.exists"
        RegisterHandlerError.EmailAlreadyExists -> "register.error.email.already.exists"
    }
}
