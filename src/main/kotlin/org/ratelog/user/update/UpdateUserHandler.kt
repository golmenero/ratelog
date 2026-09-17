package org.ratelog.user.update

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class UpdateUserCommand(
    val currentUser: User,
    val targetUserId: User.Id,
    val newUsername: Username,
    val newEmail: Email,
    val newPassword: Password?,
    val newRole: Role,
)

@Component
class UpdateUserHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun handle(command: UpdateUserCommand): Either<UpdateUserHandlerError, User> = either {
        ensure(command.currentUser.role.isAdminLike) { UpdateUserHandlerError.Forbidden }

        val target = userRepository.findById(command.targetUserId)
            ?: raise(UpdateUserHandlerError.UserNotFound)

        val existingByUsername = userRepository.findByUsername(command.newUsername)
        ensure(existingByUsername == null || existingByUsername.id == command.targetUserId) {
            UpdateUserHandlerError.UsernameAlreadyExists
        }

        val existingByEmail = userRepository.findByEmail(command.newEmail)
        ensure(existingByEmail == null || existingByEmail.id == command.targetUserId) {
            UpdateUserHandlerError.EmailAlreadyExists
        }

        if (command.newRole != target.role) {
            ensure(command.newRole != Role.SUPERADMIN) { UpdateUserHandlerError.CannotPromoteToSuperadmin }
            ensure(target.role != Role.SUPERADMIN) { UpdateUserHandlerError.CannotChangeSuperadminRole }
            ensure(command.currentUser.id != command.targetUserId) {
                UpdateUserHandlerError.CannotDemoteYourself
            }
        }

        val newHash = command.newPassword?.value?.let(passwordEncoder::encode) ?: target.passwordHash

        userRepository.updateCredentials(command.targetUserId, command.newUsername, command.newEmail, newHash)
        if (command.newRole != target.role) {
            userRepository.updateRole(command.targetUserId, command.newRole)
        }

        userRepository.findById(command.targetUserId)!!
    }
}