package org.ratelog.user.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Role
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class DeleteUserCommand(
    val currentUser: User,
    val targetUserId: User.Id,
)

@Component
class DeleteUserHandler(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun handle(command: DeleteUserCommand): Either<DeleteUserHandlerError, Unit> = either {
        ensure(command.currentUser.role.isAdminLike) { DeleteUserHandlerError.Forbidden }
        ensure(command.currentUser.id != command.targetUserId) { DeleteUserHandlerError.CannotDeleteYourself }

        val target = userRepository.findById(command.targetUserId) ?: raise(DeleteUserHandlerError.UserNotFound)
        ensure(target.role != Role.SUPERADMIN) {
            DeleteUserHandlerError.CannotDeleteSuperadmin
        }

        userRepository.deleteById(command.targetUserId)
    }
}