package org.raterr.user.feed

import org.raterr.Username
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.raterr.user.followed.FollowedUsersHandler
import org.raterr.user.followed.FollowedUsersQuery
import org.raterr.user.search.UserSearchHandler
import org.raterr.user.search.UserSearchHandlerError
import org.raterr.user.search.UserSearchQuery
import org.raterr.user.search.UserSearchResult
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class UserSearchResponse(
    val id: Long,
    val username: String,
    val isFollowed: Boolean,
    val followedAtEpochMs: Long?
)

@Controller
class FeedController(
    private val feedHandler: FeedHandler,
    private val userSearchHandler: UserSearchHandler,
    private val followedUsersHandler: FollowedUsersHandler,
) {

    @GetMapping("/community")
    fun communityPage(
        @CurrentUser user: User,
        @RequestParam("username", required = false) username: String?,
        model: Model
    ): String {
        FeedQuery(user.id!!).let(feedHandler::handle)
            .fold(
                { },
                { model.addAttribute("feed", it) }
            )

        FollowedUsersQuery(user.id).let(followedUsersHandler::handle)
            .fold(
                { },
                { model.addAttribute("followedUsers", it) }
            )

        if (!username.isNullOrBlank()) {
            UserSearchQuery(
                username = username.let(::Username),
                followerId = user.id,
                )
                .let(userSearchHandler::handle)
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
                        model.addAttribute("users", it.map(::toResponse))
                        model.addAttribute("searchedUsername", username)
                    }
                )
        }
        return "community"
    }
    
    private fun toResponse(result: UserSearchResult) = UserSearchResponse(
        id = result.id,
        username = result.username.value,
        isFollowed = result.isFollowed,
        followedAtEpochMs = result.followedAtEpochMs,
    )
}
