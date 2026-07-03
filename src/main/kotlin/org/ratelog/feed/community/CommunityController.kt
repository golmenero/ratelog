package org.ratelog.feed.community

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CommunityController(
    private val communityHandler: CommunityHandler,
) {
    @GetMapping("/community")
    fun communityPage(
        @CurrentUser user: User,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        model: Model
    ): String {
        FeedQuery(user.id!!, limit).let(communityHandler::handle)
            .fold(
                { },
                {
                    model.addAttribute("feed", it.feed)
                    model.addAttribute("hasMore", it.hasMore)
                }
            )

        model.addAttribute("limit", limit)
        return "community"
    }
}
