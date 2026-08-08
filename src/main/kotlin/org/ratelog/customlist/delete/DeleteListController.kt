package org.ratelog.customlist.delete

import org.ratelog.annotations.CurrentUser
import org.ratelog.customlist.CustomList
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class DeleteListController(
    private val handler: DeleteListHandler
) {
    @PostMapping("/list/{id}/delete")
    fun deleteList(
        @PathVariable id: Long,
        @CurrentUser user: User,
        redirectAttributes: RedirectAttributes
    ): String {
        val command = DeleteListCommand(CustomList.Id(id), user.id!!)
        return handler.handle(command).fold(
            { error ->
                val message = when (error) {
                    is DeleteListError.ListNotFound -> "list.error.not.found"
                    is DeleteListError.NotOwner -> "list.error.not.owner"
                }
                redirectAttributes.addFlashAttribute("error", message)
                "redirect:/profile/" + user.id!!.value
            },
            { "redirect:/profile/" + user.id!!.value }
        )
    }
}
