package org.ratelog.customlist.getlistdetail

import org.ratelog.annotations.CurrentUser
import org.ratelog.customlist.CustomList
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class GetListDetailController(
    private val handler: GetListDetailHandler
) {
    @GetMapping("/list/{id}")
    fun getListDetail(
        @PathVariable id: Long,
        @CurrentUser user: User?,
        model: Model
    ): String {
        val query = GetListDetailQuery(CustomList.Id(id), user?.id)
        return handler.handle(query).fold(
            { error ->
                val message = when (error) {
                    is GetListDetailError.ListNotFound -> "list.not.found"
                    is GetListDetailError.NotAuthorized -> "list.not.authorized"
                }
                model.addAttribute("error", message)
                "list-detail"
            },
            { list ->
                model.addAttribute("list", list)
                model.addAttribute("isOwner", user?.id == list.userId)
                "list-detail"
            }
        )
    }
}
