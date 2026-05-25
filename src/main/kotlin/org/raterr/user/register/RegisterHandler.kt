package org.raterr.user.register

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.Email
import org.raterr.Password
import org.raterr.Username
import org.raterr.rating.RatingRepository
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

data class RegisterUser(
    val username: Username,
    val email: Email,
    val password: Password,
)

@Component
class RegisterHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun handle(command: RegisterUser): Either<RegisterHandlerError, Unit> = either {
        ensure(command.username.value.isNotBlank() && command.email.value.isNotBlank() && command.password.value.isNotBlank()) {
            RegisterHandlerError.EmptyFields
        }

        ensure(command.username.value.length in 3..50) { RegisterHandlerError.InvalidUsernameLength }
        ensure(command.password.value.length >= 8) { RegisterHandlerError.InvalidPasswordLength }

        ensure(userRepository.findByUsername(command.username) != null) { RegisterHandlerError.UsernameAlreadyExists }
        ensure(userRepository.findByEmail(command.email) != null) { RegisterHandlerError.EmailAlreadyExists }

        val hashedPassword = command.password.value.let(passwordEncoder::encode)

        User(
            id = null,
            username = command.username,
            email = command.email,
            passwordHash = hashedPassword
        ).let(userRepository::save)
    }
}
