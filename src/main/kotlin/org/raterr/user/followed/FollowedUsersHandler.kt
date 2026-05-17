package org.raterr.user.followed

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.UserId
import org.raterr.userfollow.UserFollowRepository
import org.springframework.stereotype.Service

data class FollowedUsersQuery(
    val userId: UserId
)

data class FollowedUserResult(
    val id: Long,
    val username: String
)

@Service
class FollowedUsersHandler(
    private val userFollowRepository: UserFollowRepository
) {
    fun handle(query: FollowedUsersQuery): Either<FollowedUsersHandlerError, List<FollowedUserResult>> = either {
        val following = userFollowRepository.findFollowingByUserId(query.userId.value)
        following.map {
            FollowedUserResult(it.followedId, it.followedUsername)
        }
    }
}

sealed interface FollowedUsersHandlerError
