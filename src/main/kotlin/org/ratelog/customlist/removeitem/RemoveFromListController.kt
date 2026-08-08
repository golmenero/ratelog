package org.ratelog.customlist.removeitem

import org.ratelog.annotations.CurrentUser
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListItem
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class RemoveFromListController(
    private val handler: RemoveFromListHandler
) {
    @DeleteMapping("/list/{id}/item/{itemId}")
    fun removeItem(
        @PathVariable id: Long,
        @PathVariable itemId: Long,
        @CurrentUser user: User,
        redirectAttributes: RedirectAttributes
    ): String {
        val command = RemoveFromListCommand(CustomList.Id(id), user.id!!, CustomListItem.Id(itemId))
        return handler.handle(command).fold(
            { error ->
                val message = when (error) {
                    is RemoveFromListError.ListNotFound -> "list.error.not.found"
                    is RemoveFromListError.NotOwner -> "list.error.not.owner"
                    is RemoveFromListError.ItemNotFound -> "list.error.item.not.found"
                }
                redirectAttributes.addFlashAttribute("error", message)
                "redirect:/list/$id"
            },
            { "redirect:/list/$id" }
        )
    }
}
