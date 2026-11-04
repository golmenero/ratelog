package org.ratelog.user.edit

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
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

private data class EditUserInput(
    val username: Username,
    val email: Email,
    val currentPassword: Password?,
    val newPassword: Password?,
    val role: Role?,
)

@Controller
class EditUserController(
    private val handler: EditUserHandler,
) {

    @PostMapping("/edit-profile")
    fun editProfile(
        @CurrentUser user: User,
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("currentPassword", required = false) currentPassword: String?,
        @RequestParam("newPassword", required = false) newPassword: String?,
        redirectAttributes: RedirectAttributes,
    ): String {
        val profilePath = "/profile/${user.id!!.value}"

        val input = parseInput(username, email, currentPassword, newPassword, null).getOrElse { errorKey ->
            redirectAttributes.addFlashAttribute("error", errorKey)
            return "redirect:$profilePath"
        }

        return EditUserCommand(
            currentUser = user,
            targetUserId = user.id!!,
            newUsername = input.username,
            newEmail = input.email,
            currentPassword = input.currentPassword,
            newPassword = input.newPassword,
            newRole = null,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                { errorKey ->
                    redirectAttributes.addFlashAttribute("error", errorKey)
                    "redirect:$profilePath"
                },
                { updated ->
                    refreshCurrentUserDetails(updated)
                    redirectAttributes.addFlashAttribute("success", "edit.success.user.updated")
                    "redirect:$profilePath"
                }
            )
    }

    @PostMapping("/admin/users/{id}")
    fun editUser(
        @CurrentUser user: User,
        @PathVariable("id") id: Long,
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("currentPassword", required = false) currentPassword: String?,
        @RequestParam("newPassword", required = false) newPassword: String?,
        @RequestParam("role", required = false) role: String?,
        redirectAttributes: RedirectAttributes,
    ): String {
        val input = parseInput(username, email, currentPassword, newPassword, role).getOrElse { errorKey ->
            redirectAttributes.addFlashAttribute("error", errorKey)
            return "redirect:/admin/configuration"
        }

        return EditUserCommand(
            currentUser = user,
            targetUserId = User.Id(id),
            newUsername = input.username,
            newEmail = input.email,
            currentPassword = input.currentPassword,
            newPassword = input.newPassword,
            newRole = input.role,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                { errorKey ->
                    redirectAttributes.addFlashAttribute("error", errorKey)
                    "redirect:/admin/configuration"
                },
                { updated ->
                    if (user.id!!.value == id) {
                        refreshCurrentUserDetails(updated)
                    }
                    redirectAttributes.addFlashAttribute("success", "admin.success.user.updated")
                    "redirect:/admin/configuration"
                }
            )
    }

    private fun parseInput(
        username: String,
        email: String,
        currentPassword: String?,
        newPassword: String?,
        role: String?,
    ): Either<String, EditUserInput> = either {
        val parsedUsername = Username.parse(username).getOrElse {
            raise("edit.error.invalid.username.format")
        }

        val parsedEmail = Email.parse(email).getOrElse {
            raise("edit.error.invalid.email.format")
        }

        val parsedCurrentPassword = currentPassword?.trim()?.takeIf { it.isNotEmpty() }?.let {
            Password(it)
        }

        val parsedNewPassword = newPassword?.trim()?.takeIf { it.isNotEmpty() }?.let {
            Password.parse(it).getOrElse {
                raise("edit.error.invalid.new.password")
            }
        }

        val parsedRole = role?.trim()?.takeIf { it.isNotEmpty() }?.let {
            Role.parse(it).getOrElse {
                raise("admin.error.invalid.role")
            }
        }

        EditUserInput(
            username = parsedUsername,
            email = parsedEmail,
            currentPassword = parsedCurrentPassword,
            newPassword = parsedNewPassword,
            role = parsedRole,
        )
    }

    private fun refreshCurrentUserDetails(updated: User) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentDetails = auth?.principal as? AppUserDetails ?: return
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

    private fun mapError(error: EditUserHandlerError): String = when (error) {
        EditUserHandlerError.Forbidden -> "admin.error.forbidden"
        EditUserHandlerError.UserNotFound -> "edit.error.user.not.found"
        EditUserHandlerError.UsernameAlreadyExists -> "edit.error.username.already.exists"
        EditUserHandlerError.EmailAlreadyExists -> "edit.error.email.already.exists"
        EditUserHandlerError.MissingCurrentPassword -> "edit.error.missing.current.password"
        EditUserHandlerError.InvalidCurrentPassword -> "edit.error.invalid.current.password"
        EditUserHandlerError.CannotEditAdmin -> "admin.error.cannot.edit.admin"
        EditUserHandlerError.CannotChangeRole -> "admin.error.cannot.change.role"
        EditUserHandlerError.CannotPromoteToSuperadmin -> "admin.error.cannot.promote.superadmin"
        EditUserHandlerError.CannotChangeSuperadminRole -> "admin.error.cannot.change.superadmin.role"
        EditUserHandlerError.CannotDemoteYourself -> "admin.error.cannot.demote.self"
    }
}
