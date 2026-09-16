package org.ratelog.admin.listusers

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ListUsersController(
    private val handler: ListUsersHandler,
) {

    @GetMapping("/admin/dashboard")
    fun dashboard(
        @CurrentUser currentUser: User,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String {
        val users = handler.handle(ListUsersQuery(currentUser))
            .mapLeft { error ->
                when (error) {
                    ListUsersHandlerError.Forbidden -> "admin.error.forbidden"
                }
            }
            .fold(
                { msg ->
                    redirectAttributes.addFlashAttribute("error", msg)
                    emptyList()
                },
                { it }
            )
        model.addAttribute("users", users)
        model.addAttribute("currentUserId", currentUser.id!!.value)
        return "admin/dashboard"
    }
}
