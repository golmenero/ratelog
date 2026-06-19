package org.ratelog.user.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

sealed interface ToggleUserFollowHandlerError {
    data object CannotFollowYourself : ToggleUserFollowHandlerError
    data object UserNotFound : ToggleUserFollowHandlerError
}

data class ToggleUserFollow(
    val followerId: User.Id,
    val followedId: User.Id,
)

@Service
class ToggleUserFollowHandler(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun handle(command: ToggleUserFollow): Either<ToggleUserFollowHandlerError, Unit> = either {
        val followerId = command.followerId
        val followedUser = userRepository.findById(command.followedId)
            ?: raise(ToggleUserFollowHandlerError.UserNotFound)

        if (followerId == followedUser.id) {
            raise(ToggleUserFollowHandlerError.CannotFollowYourself)
        }

        userRepository.toggleFollow(command.followerId, command.followedId)
    }
}
