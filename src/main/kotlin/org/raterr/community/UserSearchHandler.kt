package org.raterr.community

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.UserId
import org.raterr.follow.UserFollowRepository
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service

data class UserSearchQuery(
    val username: String,
    val followerId: UserId?
)

data class UserSearchResult(
    val id: Long,
    val username: String,
    val isFollowed: Boolean
)

@Service
class UserSearchHandler(
    private val userRepository: UserRepository,
    private val userFollowRepository: UserFollowRepository
) {
    fun handle(query: UserSearchQuery): Either<UserSearchHandlerError, List<UserSearchResult>> = either {
        if (query.username.isBlank()) {
            raise(UserSearchHandlerError.EmptyQuery)
        }

        val users = userRepository.findByUsernameContaining(query.username)
            .map { user ->
                val isFollowed = query.followerId?.value?.let { followerId ->
                    userFollowRepository.existsByFollowerIdAndFollowedId(followerId, user.id!!)
                } ?: false

                UserSearchResult(user.id!!, user.username, isFollowed)
            }

        if (users.isEmpty()) {
            raise(UserSearchHandlerError.NoUsersFound(query.username))
        }

        users
    }
}
