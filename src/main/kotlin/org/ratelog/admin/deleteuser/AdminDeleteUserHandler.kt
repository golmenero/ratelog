package org.ratelog.admin.deleteuser

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Role
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class AdminDeleteUserCommand(
    val currentUser: User,
    val targetUserId: User.Id,
)

@Component
class AdminDeleteUserHandler(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun handle(command: AdminDeleteUserCommand): Either<AdminDeleteUserHandlerError, Unit> = either {
        ensure(command.currentUser.role.isAdminLike) { AdminDeleteUserHandlerError.Forbidden }
        ensure(command.currentUser.id != command.targetUserId) { AdminDeleteUserHandlerError.CannotDeleteYourself }

        val target = userRepository.findById(command.targetUserId) ?: raise(AdminDeleteUserHandlerError.UserNotFound)
        ensure(!(target.role == Role.SUPERADMIN && command.currentUser.role != Role.SUPERADMIN)) {
            AdminDeleteUserHandlerError.CannotDeleteSuperadmin
        }

        userRepository.deleteById(command.targetUserId)
    }
}
