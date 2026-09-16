package org.ratelog.admin.updaterole

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Role
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class AdminUpdateRoleCommand(
    val currentUser: User,
    val targetUserId: User.Id,
    val newRole: Role,
)

@Component
class AdminUpdateRoleHandler(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun handle(command: AdminUpdateRoleCommand): Either<AdminUpdateRoleHandlerError, User> = either {
        ensure(command.currentUser.role.isAdminLike) { AdminUpdateRoleHandlerError.Forbidden }

        val target = userRepository.findById(command.targetUserId) ?: raise(AdminUpdateRoleHandlerError.UserNotFound)

        ensure(!(command.newRole == Role.SUPERADMIN && command.currentUser.role != Role.SUPERADMIN)) {
            AdminUpdateRoleHandlerError.CannotPromoteToSuperadmin
        }
        ensure(!(target.role == Role.SUPERADMIN && command.currentUser.role != Role.SUPERADMIN)) {
            AdminUpdateRoleHandlerError.CannotChangeSuperadminRole
        }
        ensure(!(command.currentUser.id == command.targetUserId && command.newRole != command.currentUser.role)) {
            AdminUpdateRoleHandlerError.CannotDemoteYourself
        }

        userRepository.updateRole(command.targetUserId, command.newRole)
        userRepository.findById(command.targetUserId)!!
    }
}
