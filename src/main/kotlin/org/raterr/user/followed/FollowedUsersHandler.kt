package org.raterr.user.followed

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service

data class FollowedUsersQuery(
    val userId: User.Id
)

data class FollowedUserResult(
    val id: Long,
    val username: String
)

@Service
class FollowedUsersHandler(
    private val userRepository: UserRepository
) {
    fun handle(query: FollowedUsersQuery): Either<FollowedUsersHandlerError, List<FollowedUserResult>> = either {
        val following = userRepository.findFollowingByUserId(query.userId)
        following.map {
            FollowedUserResult(it.id!!.value, it.username.value)
        }
    }
}

sealed interface FollowedUsersHandlerError
