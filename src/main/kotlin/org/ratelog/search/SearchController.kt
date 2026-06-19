package org.ratelog.search

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class SearchController(
    private val handler: SearchHandler,
) {

    @GetMapping("/")
    fun searchPage(
        @CurrentUser user: User,
        @RequestParam("q", required = false) query: String?,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (!query.isNullOrBlank()) {
            SearchQuery(
                query = query,
                userId = user.id!!,
                lang = user.lang,
            ).let(handler::handle)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                },
                {
                    model.addAttribute("query", query)
                    model.addAttribute("results", it)
                }
            )
        }
        return "search"
    }
}
