package org.ratelog.admin.updatecredentials

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class AdminUpdateCredentialsCommand(
    val currentUser: User,
    val targetUserId: User.Id,
    val newUsername: Username,
    val newPassword: Password?,
)

@Component
class AdminUpdateCredentialsHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun handle(command: AdminUpdateCredentialsCommand): Either<AdminUpdateCredentialsHandlerError, User> = either {
        ensure(command.currentUser.role.isAdminLike) { AdminUpdateCredentialsHandlerError.Forbidden }

        val target = userRepository.findById(command.targetUserId) ?: raise(AdminUpdateCredentialsHandlerError.UserNotFound)

        val existingByUsername = userRepository.findByUsername(command.newUsername)
        ensure(existingByUsername == null || existingByUsername.id == command.targetUserId) {
            AdminUpdateCredentialsHandlerError.UsernameAlreadyExists
        }

        val newHash = command.newPassword?.value?.let(passwordEncoder::encode) ?: target.passwordHash

        userRepository.updateCredentials(command.targetUserId, command.newUsername, newHash)
        userRepository.findById(command.targetUserId)!!
    }
}
