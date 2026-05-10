package org.raterr.search

import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchController(
    private val handler: SearchHandler,
) {

    @GetMapping("/")
    fun searchPage(
        @CurrentUser user: User?,
        @RequestParam("q", required = false) query: String?,
        model: Model
    ): String {
        if (!query.isNullOrBlank()) {
            val results = SearchQuery(
                query = query,
                userId = user?.id?.let(::UserId),
            ).let(handler::handle)

            model.addAttribute("query", query)
            model.addAttribute("results", results)
            model.addAttribute("currentUser", user)
        }
        return "search"
    }
}
