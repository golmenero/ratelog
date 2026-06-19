package org.ratelog.user.feed

import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.ratelog.user.followed.FollowedUsersHandler
import org.ratelog.user.followed.FollowedUsersQuery
import org.ratelog.user.search.UserSearchHandler
import org.ratelog.user.search.UserSearchHandlerError
import org.ratelog.user.search.UserSearchQuery
import org.ratelog.user.search.UserSearchResult
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

data class UserSearchResponse(
    val id: Long,
    val username: String,
)

@Controller
class FeedController(
    private val feedHandler: FeedHandler,
    private val userSearchHandler: UserSearchHandler,
    private val followedUsersHandler: FollowedUsersHandler,
) {

    @GetMapping("/users/search")
    @ResponseBody
    fun searchUsersApi(
        @CurrentUser user: User,
        @RequestParam("q") query: String?
    ): List<UserSearchResponse> {
        if (query.isNullOrBlank()) return emptyList()

        return UserSearchQuery(
            username = Username(query),
            followerId = user.id
        )
            .let(userSearchHandler::handle)
            .fold(
                { emptyList() },
                { it.map(::toResponse) }
            )
    }

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
    )
}
