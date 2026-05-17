package org.raterr.userfollow.toggleuser

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.UserId
import org.raterr.userfollow.UserFollow
import org.raterr.userfollow.UserFollowRepository
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

sealed interface ToggleUserFollowHandlerError {
    data object CannotFollowYourself : ToggleUserFollowHandlerError
    data object UserNotFound : ToggleUserFollowHandlerError
}

data class ToggleUserFollow(
    val followerId: UserId,
    val followedUsername: String,
)

@Service
class ToggleUserFollowHandler(
    private val userFollowRepository: UserFollowRepository,
    private val userRepository: UserRepository,
) {

    fun handle(command: ToggleUserFollow): Either<ToggleUserFollowHandlerError, Unit> = either {
        val followerId = command.followerId.value
        val followedUser = userRepository.findByUsername(command.followedUsername)
            .getOrNull() ?: raise(ToggleUserFollowHandlerError.UserNotFound)
        val followedId = followedUser.id!!

        if (followerId == followedId) {
            raise(ToggleUserFollowHandlerError.CannotFollowYourself)
        }

        val existingFollow = userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId)
        if (existingFollow) {
            userFollowRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .ifPresent(userFollowRepository::delete)
        } else {
            UserFollow(
                followerId = followerId,
                followedId = followedId,
            ).let(userFollowRepository::save)
        }
    }
}
