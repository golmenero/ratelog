package org.ratelog.admin.updaterole

import arrow.core.getOrElse
import org.ratelog.Role
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
class AdminUpdateRoleController(
    private val handler: AdminUpdateRoleHandler,
) {

    @PostMapping("/admin/users/{id}/role")
    fun update(
        @CurrentUser currentUser: User,
        @PathVariable("id") id: Long,
        @RequestParam("role") role: String,
        redirectAttributes: RedirectAttributes,
    ): String {
        val parsedRole = Role.parse(role).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.role")
            return "redirect:/admin/dashboard"
        }
        return AdminUpdateRoleCommand(currentUser, User.Id(id), parsedRole)
            .let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                { msg ->
                    redirectAttributes.addFlashAttribute("error", msg)
                    "redirect:/admin/dashboard"
                },
                { updated ->
                    if (currentUser.id!!.value == id) {
                        refreshCurrentUserDetails(updated)
                    }
                    redirectAttributes.addFlashAttribute("success", "admin.success.user.role.updated")
                    "redirect:/admin/dashboard"
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
                username = currentDetails.username,
                email = currentDetails.email,
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

    private fun mapError(error: AdminUpdateRoleHandlerError): String = when (error) {
        AdminUpdateRoleHandlerError.Forbidden -> "admin.error.forbidden"
        AdminUpdateRoleHandlerError.UserNotFound -> "admin.error.user.not.found"
        AdminUpdateRoleHandlerError.CannotPromoteToSuperadmin -> "admin.error.cannot.promote.superadmin"
        AdminUpdateRoleHandlerError.CannotChangeSuperadminRole -> "admin.error.cannot.change.superadmin.role"
        AdminUpdateRoleHandlerError.CannotDemoteYourself -> "admin.error.cannot.demote.self"
    }
}
