package org.ratelog.user.community

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CommunityController(
    private val communityHandler: CommunityHandler,
    private val followedUsersHandler: FollowedUsersHandler,
) {
    @GetMapping("/community")
    fun communityPage(
        @CurrentUser user: User,
        model: Model
    ): String {
        FeedQuery(user.id!!).let(communityHandler::handle)
            .fold(
                { },
                { model.addAttribute("feed", it) }
            )

        FollowedUsersQuery(user.id).let(followedUsersHandler::handle)
            .fold(
                { },
                { model.addAttribute("followedUsers", it) }
            )
        return "community"
    }
}
