package org.ratelog.user.register

import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Username
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Locale
import jakarta.servlet.http.HttpServletRequest
import org.ratelog.user.BrowserLangResolver

@Controller
class RegisterController(
    private val handler: RegisterHandler,
    private val messageSource: MessageSource,
) {

    @GetMapping("/register")
    fun registerPage(): String = "register"

    @PostMapping("/register")
    fun register(
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        redirectAttributes: RedirectAttributes,
        locale: Locale,
        request: HttpServletRequest
    ): String {
        val browserLang = BrowserLangResolver.resolve(request)

        return RegisterUser(
            username = username.let(::Username),
            email = email.let(::Email),
            password = password.let(::Password),
            lang = Lang.valueOf(browserLang.language),
        ).let(handler::handle)
            .mapLeft { mapError(it, locale) }
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/register"
                },
                { "redirect:/login" }
            )
    }

    private fun mapError(error: RegisterHandlerError, locale: Locale): String = when (error) {
        RegisterHandlerError.EmptyFields -> messageSource.getMessage("register.error.empty.fields", null, locale)
        RegisterHandlerError.InvalidUsernameLength -> messageSource.getMessage("register.error.invalid.username.length", null, locale)
        RegisterHandlerError.InvalidPasswordLength -> messageSource.getMessage("register.error.invalid.password.length", null, locale)
        RegisterHandlerError.UsernameAlreadyExists -> messageSource.getMessage("register.error.username.already.exists", null, locale)
        RegisterHandlerError.EmailAlreadyExists -> messageSource.getMessage("register.error.email.already.exists", null, locale)
    }
}
