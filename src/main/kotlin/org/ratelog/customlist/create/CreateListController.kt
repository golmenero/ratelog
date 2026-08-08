package org.ratelog.customlist.create

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class CreateListController(
    private val handler: CreateListHandler
) {
    @PostMapping("/list")
    fun createList(
        @CurrentUser user: User,
        @RequestParam name: String,
        @RequestParam(name = "isPublic", required = false, defaultValue = "false") isPublic: Boolean,
        redirectAttributes: RedirectAttributes
    ): String {
        val command = CreateListCommand(user.id!!, name, isPublic)
        return handler.handle(command).fold(
            { error ->
                val message = when (error) {
                    is CreateListError.InvalidListName -> "list.error.invalid.name"
                    is CreateListError.ListLimitExceeded -> "list.error.limit.exceeded"
                }
                redirectAttributes.addFlashAttribute("error", message)
                "redirect:/lists"
            },
            { list -> "redirect:/list/${list.id!!.value}" }
        )
    }
}
