package org.ratelog.feed.community

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    @Transactional
    fun handle(query: FollowedUsersQuery): Either<FollowedUsersHandlerError, List<FollowedUserResult>> = either {
        val following = userRepository.findFollowingByUserId(query.userId)
        following.map {
            FollowedUserResult(it.id!!.value, it.username.value)
        }
    }
}

sealed interface FollowedUsersHandlerError
