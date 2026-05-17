package org.raterr.user.feed

import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.raterr.user.followed.FollowedUsersHandler
import org.raterr.user.followed.FollowedUsersQuery
import org.raterr.user.search.UserSearchHandler
import org.raterr.user.search.UserSearchHandlerError
import org.raterr.user.search.UserSearchQuery
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class FeedController(
    private val feedHandler: FeedHandler,
    private val userSearchHandler: UserSearchHandler,
    private val followedUsersHandler: FollowedUsersHandler,
) {

    @GetMapping("/community")
    fun communityPage(
        @CurrentUser user: User?,
        @RequestParam("username", required = false) username: String?,
        model: Model
    ): String {
        if (user != null) {
            FeedQuery(UserId(user.id!!)).let(feedHandler::handle)
                .fold(
                    { },
                    { model.addAttribute("feed", it) }
                )

            FollowedUsersQuery(UserId(user.id)).let(followedUsersHandler::handle)
                .fold(
                    { },
                    { model.addAttribute("followedUsers", it) }
                )
        }

        if (!username.isNullOrBlank() && user != null) {
            UserSearchQuery(
                username = username,
                followerId = UserId(user.id!!)
            ).let(userSearchHandler::handle)
                .fold(
                    {
                        when (it) {
                            is UserSearchHandlerError.EmptyQuery -> {
                                model.addAttribute("followError", "Query cannot be empty")
                            }
                            is UserSearchHandlerError.NoUsersFound -> {
                                model.addAttribute("followError", "No users found for '${it.username}'")
                            }
                        }
                    },
                    {
                        model.addAttribute("users", it)
                        model.addAttribute("searchedUsername", username)
                    }
                )
        }
        return "community"
    }
}
