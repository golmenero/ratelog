package org.ratelog.user.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
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
    private val userRepository: UserRepository,
) {

    fun handle(command: ToggleUserFollow): Either<ToggleUserFollowHandlerError, Unit> = either {
        val followerId = command.followerId
        val followedUser = userRepository.findByUsername(command.followedUsername)
            ?: raise(ToggleUserFollowHandlerError.UserNotFound)

        if (followerId == followedUser.id) {
            raise(ToggleUserFollowHandlerError.CannotFollowYourself)
        }

        followedUser.toggleFollow(System.currentTimeMillis()).let(userRepository::save)
    }
}
