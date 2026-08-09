package org.ratelog.customlist.additem

import org.ratelog.annotations.CurrentUser
import org.ratelog.customlist.CustomList
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AddToListController(
    private val handler: AddToListHandler
) {
    @PostMapping("/list/{id}/item")
    fun addItem(
        @PathVariable id: Long,
        @CurrentUser user: User,
        @RequestParam mediaId: Long,
        @RequestParam mediaType: String,
        redirectAttributes: RedirectAttributes
    ): String {
        val command = AddToListCommand(CustomList.Id(id), user.id!!, mediaId, mediaType)
        return handler.handle(command).fold(
            { error ->
                val message = when (error) {
                    is AddToListError.ListNotFound -> "list.error.not.found"
                    is AddToListError.NotOwner -> "list.error.not.owner"
                    is AddToListError.ItemAlreadyInList -> "list.error.item.already.in.list"
                }
                redirectAttributes.addFlashAttribute("error", message)
                "redirect:/list/$id"
            },
            { "redirect:/list/$id" }
        )
    }
}
