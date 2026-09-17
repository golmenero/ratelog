package org.ratelog.admin.updatecredentials

import arrow.core.getOrElse
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.AppUserDetails
import org.ratelog.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AdminUpdateCredentialsController(
    private val handler: AdminUpdateCredentialsHandler,
) {

    @PostMapping("/admin/users/{id}/credentials")
    fun update(
        @CurrentUser currentUser: User,
        @PathVariable("id") id: Long,
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password", required = false) password: String?,
        redirectAttributes: RedirectAttributes,
    ): String {
        val parsedUsername = Username.parse(username).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.username")
            return "redirect:/admin/dashboard"
        }
        val parsedEmail = Email.parse(email).getOrElse {
            redirectAttributes.addFlashAttribute("error", "admin.error.invalid.email")
            return "redirect:/admin/dashboard"
        }
        val parsedPassword = password?.ifEmpty { null }?.let {
            Password.parse(it).getOrElse {
                redirectAttributes.addFlashAttribute("error", "admin.error.invalid.password")
                return "redirect:/admin/dashboard"
            }
        }

        return AdminUpdateCredentialsCommand(
            currentUser = currentUser,
            targetUserId = User.Id(id),
            newUsername = parsedUsername,
            newEmail = parsedEmail,
            newPassword = parsedPassword,
        ).let(handler::handle)
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
                    redirectAttributes.addFlashAttribute("success", "admin.success.user.updated")
                    "redirect:/admin/dashboard"
                }
            )
    }

    private fun refreshCurrentUserDetails(updated: User) {
        val auth = SecurityContextHolder.getContext().authentication
        val currentDetails = auth.principal as? AppUserDetails ?: return
        val newAuth = UsernamePasswordAuthenticationToken(
            AppUserDetails(
                id = currentDetails.id,
                username = updated.username.value,
                email = updated.email.value,
                password = updated.passwordHash,
                lang = updated.lang,
                metadataLang = updated.metadataLang,
                role = currentDetails.role,
            ),
            auth.credentials,
            auth.authorities
        )
        SecurityContextHolder.getContext().authentication = newAuth
    }

    private fun mapError(error: AdminUpdateCredentialsHandlerError): String = when (error) {
        AdminUpdateCredentialsHandlerError.Forbidden -> "admin.error.forbidden"
        AdminUpdateCredentialsHandlerError.UserNotFound -> "admin.error.user.not.found"
        AdminUpdateCredentialsHandlerError.UsernameAlreadyExists -> "admin.error.username.exists"
        AdminUpdateCredentialsHandlerError.EmailAlreadyExists -> "admin.error.email.exists"
    }
}
