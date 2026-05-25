package org.raterr.user.search

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Username
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service

data class UserSearchQuery(
    val username: Username,
    val followerId: User.Id?
)

data class UserSearchResult(
    val id: Long,
    val username: Username,
    val isFollowed: Boolean,
    val followedAtEpochMs: Long?
)

@Service
class UserSearchHandler(
    private val userRepository: UserRepository
) {
    fun handle(query: UserSearchQuery): Either<UserSearchHandlerError, List<UserSearchResult>> = either {
        if (query.username.value.isBlank()) {
            raise(UserSearchHandlerError.EmptyQuery)
        }

        val users = query.followerId?.let { followerId ->
            userRepository.findByUsernameContaining(query.username, followerId)
        } ?: userRepository.findByUsernameContaining(query.username)

        val results = users.map { user ->
            UserSearchResult(user.id!!.value, user.username, user.followed, user.followedAtEpochMs)
        }

        if (results.isEmpty()) {
            raise(UserSearchHandlerError.NoUsersFound(query.username.value))
        }

        results
    }
}
