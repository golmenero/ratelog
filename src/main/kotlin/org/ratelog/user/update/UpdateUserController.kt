package org.ratelog.user.update

import arrow.core.getOrElse
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.AppUserDetails
import org.ratelog.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class UpdateUserController(
    private val handler: UpdateUserHandler,
) {

    @PostMapping("/admin/users/{id}")
    fun update(
        @CurrentUser currentUser: User,
        @PathVariable("id") id: Long,
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password", required = false) password: String?,
        @RequestParam("role") role: String,
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
        val parsedPassword = password?.ifEmpty { null }?.let {
            Password.parse(it).getOrElse {
                redirectAttributes.addFlashAttribute("error", "admin.error.invalid.password")
                return "redirect:/admin/configuration"
            }
        }
        val parsedRole = Role.parse(role).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.role")
            return "redirect:/admin/configuration"
        }

        return UpdateUserCommand(
            currentUser = currentUser,
            targetUserId = User.Id(id),
            newUsername = parsedUsername,
            newEmail = parsedEmail,
            newPassword = parsedPassword,
            newRole = parsedRole,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                { msg ->
                    redirectAttributes.addFlashAttribute("error", msg)
                    "redirect:/admin/configuration"
                },
                { updated ->
                    if (currentUser.id!!.value == id) {
                        refreshCurrentUserDetails(updated)
                    }
                    redirectAttributes.addFlashAttribute("success", "admin.success.user.updated")
                    "redirect:/admin/configuration"
                }
            )
    }

    private fun refreshCurrentUserDetails(updated: User) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentDetails = auth.principal as? AppUserDetails ?: return
        val authorities: Collection<GrantedAuthority> = when (updated.role) {
            Role.SUPERADMIN -> listOf(
                SimpleGrantedAuthority(Role.USER.authority),
                SimpleGrantedAuthority(Role.ADMIN.authority),
                SimpleGrantedAuthority(Role.SUPERADMIN.authority),
            )
            Role.ADMIN -> listOf(
                SimpleGrantedAuthority(Role.USER.authority),
                SimpleGrantedAuthority(Role.ADMIN.authority),
            )
            Role.USER -> listOf(SimpleGrantedAuthority(Role.USER.authority))
        }
        val newAuth = UsernamePasswordAuthenticationToken(
            AppUserDetails(
                id = currentDetails.id,
                username = updated.username.value,
                email = updated.email.value,
                password = updated.passwordHash,
                lang = updated.lang,
                metadataLang = updated.metadataLang,
                role = updated.role,
            ),
            auth.credentials,
            authorities
        )
        SecurityContextHolder.getContext().authentication = newAuth
    }

    private fun mapError(error: UpdateUserHandlerError): String = when (error) {
        UpdateUserHandlerError.Forbidden -> "admin.error.forbidden"
        UpdateUserHandlerError.UserNotFound -> "admin.error.user.not.found"
        UpdateUserHandlerError.UsernameAlreadyExists -> "admin.error.username.exists"
        UpdateUserHandlerError.EmailAlreadyExists -> "admin.error.email.exists"
        UpdateUserHandlerError.CannotPromoteToSuperadmin -> "admin.error.cannot.promote.superadmin"
        UpdateUserHandlerError.CannotChangeSuperadminRole -> "admin.error.cannot.change.superadmin.role"
        UpdateUserHandlerError.CannotDemoteYourself -> "admin.error.cannot.demote.self"
        UpdateUserHandlerError.CannotChangeRole -> "admin.error.cannot.change.role"
    }
}