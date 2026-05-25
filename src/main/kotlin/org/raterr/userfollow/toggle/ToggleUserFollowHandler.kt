package org.raterr.userfollow.toggleuser

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Username
import org.raterr.user.User
import org.raterr.userfollow.UserFollow
import org.raterr.userfollow.UserFollowRepository
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service

sealed interface ToggleUserFollowHandlerError {
    data object CannotFollowYourself : ToggleUserFollowHandlerError
    data object UserNotFound : ToggleUserFollowHandlerError
}

data class ToggleUserFollow(
    val followerId: User.Id,
    val followedUsername: Username,
)

@Service
class ToggleUserFollowHandler(
    private val userFollowRepository: UserFollowRepository,
    private val userRepository: UserRepository,
) {

    fun handle(command: ToggleUserFollow): Either<ToggleUserFollowHandlerError, Unit> = either {
        val followerId = command.followerId
        val followedUser = userRepository.findByUsername(command.followedUsername)
            ?: raise(ToggleUserFollowHandlerError.UserNotFound)
        val followedId = followedUser.id!!

        if (followerId == followedId) {
            raise(ToggleUserFollowHandlerError.CannotFollowYourself)
        }

        val existingFollow = userFollowRepository.existsByFollowerIdAndFollowedId(followerId.value, followedId.value)
        if (existingFollow) {
            userFollowRepository.findByFollowerIdAndFollowedId(followerId.value, followedId.value)
                .ifPresent(userFollowRepository::delete)
        } else {
            UserFollow(
                id = null,
                followerId = followerId.value,
                followedId = followedId.value,
            ).let(userFollowRepository::save)
        }
    }
}
