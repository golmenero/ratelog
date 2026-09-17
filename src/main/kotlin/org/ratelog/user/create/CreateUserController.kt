package org.ratelog.user.create

import arrow.core.getOrElse
import jakarta.servlet.http.HttpServletRequest
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.ratelog.user.UserDetailsService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class CreateUserController(
    private val handler: CreateUserHandler,
) {

    @PostMapping("/admin/users/create")
    fun create(
        @CurrentUser currentUser: User,
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password") password: String,
        @RequestParam("lang", required = false) lang: String?,
        @RequestParam("role") role: String,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes,
    ): String {
        val parsedUsername = Username.parse(username).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.username")
            return "redirect:/admin/configuration"
        }
        val parsedEmail = Email.parse(email).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.email")
            return "redirect:/admin/configuration"
        }
        val parsedPassword = Password.parse(password).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.password")
            return "redirect:/admin/configuration"
        }
        val parsedRole = Role.parse(role).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.role")
            return "redirect:/admin/configuration"
        }
        val resolvedLang = lang?.let { runCatching { Lang.valueOf(it) }.getOrNull() }
            ?: UserDetailsService.resolve(request)

        return CreateUserCommand(
            currentUser = currentUser,
            username = parsedUsername,
            email = parsedEmail,
            password = parsedPassword,
            lang = resolvedLang,
            role = parsedRole,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                { msg ->
                    redirectAttributes.addFlashAttribute("error", msg)
                    "redirect:/admin/configuration"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "admin.success.user.created")
                    "redirect:/admin/configuration"
                }
            )
    }

    private fun mapError(error: CreateUserHandlerError): String = when (error) {
        CreateUserHandlerError.Forbidden -> "admin.error.forbidden"
        CreateUserHandlerError.UsernameAlreadyExists -> "admin.error.username.exists"
        CreateUserHandlerError.EmailAlreadyExists -> "admin.error.email.exists"
        CreateUserHandlerError.CannotPromoteToSuperadmin -> "admin.error.cannot.promote.superadmin"
        CreateUserHandlerError.CannotCreateAdmin -> "admin.error.cannot.create.admin"
    }
}