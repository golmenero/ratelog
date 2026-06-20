package org.ratelog.user.edit

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class EditUserCommand(
    val userId: User.Id,
    val username: Username,
    val email: Email,
    val currentPassword: Password,
    val newPassword: Password?,
)

@Component
class EditUserHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun handle(command: EditUserCommand): Either<EditUserHandlerError, Unit> = either {
        val user = userRepository.findById(command.userId) ?: raise(EditUserHandlerError.UserNotFound)

        ensure(passwordEncoder.matches(command.currentPassword.value, user.passwordHash)) {
            EditUserHandlerError.InvalidCurrentPassword
        }

        val existingByUsername = userRepository.findByUsername(command.username)
        ensure(existingByUsername == null || existingByUsername.id == command.userId) {
            EditUserHandlerError.UsernameAlreadyExists
        }

        val existingByEmail = userRepository.findByEmail(command.email)
        ensure(existingByEmail == null || existingByEmail.id == command.userId) {
            EditUserHandlerError.EmailAlreadyExists
        }

        val newPasswordHash = command.newPassword?.value?.let(passwordEncoder::encode) ?: user.passwordHash

        val updatedUser = user.copy(
            username = command.username,
            email = command.email,
            passwordHash = newPasswordHash,
        )

        userRepository.save(updatedUser)
    }
}
