package org.ratelog.user.search

import arrow.core.getOrElse
import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

data class UserSearchResponse(
    val id: Long,
    val username: String,
)

@Controller
class UserSearchController(
    private val userSearchHandler: UserSearchHandler,
) {
    @GetMapping("/users/search")
    @ResponseBody
    fun searchUsersApi(
        @CurrentUser user: User,
        @RequestParam("q") query: String?
    ): List<UserSearchResponse> {
        if (query.isNullOrBlank()) return emptyList()

        val parsedUsername = Username.parse(query).getOrElse { return emptyList() }
        return UserSearchQuery(
            username = parsedUsername,
            followerId = user.id
        )
            .let(userSearchHandler::handle)
            .fold(
                { emptyList() },
                { it.map(::toResponse) }
            )
    }
    
    private fun toResponse(result: UserSearchResult) = UserSearchResponse(
        id = result.id,
        username = result.username.value,
    )
}
