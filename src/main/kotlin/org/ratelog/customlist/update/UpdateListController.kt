package org.ratelog.customlist.update

import org.ratelog.annotations.CurrentUser
import org.ratelog.customlist.CustomList
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class UpdateListController(
    private val handler: UpdateListHandler
) {
    @PostMapping("/list/{id}/edit")
    fun updateList(
        @PathVariable id: Long,
        @CurrentUser user: User,
        @RequestParam name: String,
        @RequestParam(name = "isPublic", required = false, defaultValue = "false") isPublic: Boolean,
        redirectAttributes: RedirectAttributes
    ): String {
        val command = UpdateListCommand(CustomList.Id(id), user.id!!, name, isPublic)
        return handler.handle(command).fold(
            { error ->
                val message = when (error) {
                    is UpdateListError.ListNotFound -> "list.error.not.found"
                    is UpdateListError.NotOwner -> "list.error.not.owner"
                    is UpdateListError.InvalidListName -> "list.error.invalid.name"
                }
                redirectAttributes.addFlashAttribute("error", message)
                "redirect:/list/$id"
            },
            { "redirect:/list/$id" }
        )
    }
}
