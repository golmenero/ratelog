package org.ratelog.admin.deleteuser

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AdminDeleteUserController(
    private val handler: AdminDeleteUserHandler,
) {

    @PostMapping("/admin/users/{id}/delete")
    fun delete(
        @CurrentUser currentUser: User,
        @PathVariable("id") id: Long,
        redirectAttributes: RedirectAttributes,
    ): String {
        return AdminDeleteUserCommand(currentUser, User.Id(id))
            .let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                { msg ->
                    redirectAttributes.addFlashAttribute("error", msg)
                    "redirect:/admin/configuration"
                },
                {
                    if (currentUser.id!!.value == id) {
                        SecurityContextHolder.clearContext()
                        redirectAttributes.addFlashAttribute("success", "admin.success.user.deleted.self")
                        "redirect:/login?deleted=true"
                    } else {
                        redirectAttributes.addFlashAttribute("success", "admin.success.user.deleted")
                        "redirect:/admin/configuration"
                    }
                }
            )
    }

    private fun mapError(error: AdminDeleteUserHandlerError): String = when (error) {
        AdminDeleteUserHandlerError.Forbidden -> "admin.error.forbidden"
        AdminDeleteUserHandlerError.UserNotFound -> "admin.error.user.not.found"
        AdminDeleteUserHandlerError.CannotDeleteYourself -> "admin.error.cannot.delete.self"
        AdminDeleteUserHandlerError.CannotDeleteSuperadmin -> "admin.error.cannot.delete.superadmin"
    }
}
