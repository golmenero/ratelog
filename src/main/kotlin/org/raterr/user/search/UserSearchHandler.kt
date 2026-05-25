package org.raterr.user.search

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Username
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.raterr.userfollow.UserFollowRepository

data class UserSearchQuery(
    val username: Username,
    val followerId: User.Id?
)

data class UserSearchResult(
    val id: Long,
    val username: Username,
    val isFollowed: Boolean
)

class UserSearchHandler(
    private val userRepository: UserRepository,
    private val userFollowRepository: UserFollowRepository
) {
    fun handle(query: UserSearchQuery): Either<UserSearchHandlerError, List<UserSearchResult>> = either {
        if (query.username.value.isBlank()) {
            raise(UserSearchHandlerError.EmptyQuery)
        }

        val users = userRepository.findByUsernameContaining(query.username)
            .map { user ->
                val isFollowed = query.followerId?.value?.let { followerId ->
                    user.id?.value?.let { userId ->
                        userFollowRepository.existsByFollowerIdAndFollowedId(followerId, userId)
                    } ?: false
                } ?: false

                UserSearchResult(user.id!!.value, user.username, isFollowed)
            }

        if (users.isEmpty()) {
            raise(UserSearchHandlerError.NoUsersFound(query.username.value))
        }

        users
    }
}
