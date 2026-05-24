package org.raterr.user.register

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.rating.RatingRepository
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

data class RegisterUser(
    val username: String,
    val email: String,
    val password: String,
)

@Component
class RegisterHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val ratingRepository: RatingRepository,
) {
    fun handle(command: RegisterUser): Either<RegisterHandlerError, Unit> = either {
        ensure(command.username.isNotBlank() && command.email.isNotBlank() && command.password.isNotBlank()) {
            RegisterHandlerError.EmptyFields
        }

        ensure(command.username.length in 3..50) { RegisterHandlerError.InvalidUsernameLength }
        ensure(command.password.length >= 8) { RegisterHandlerError.InvalidPasswordLength }

        ensure(!userRepository.existsByUsername(command.username)) { RegisterHandlerError.UsernameAlreadyExists }
        ensure(!userRepository.existsByEmail(command.email)) { RegisterHandlerError.EmailAlreadyExists }

        val hashedPassword = passwordEncoder.encode(command.password)

        val user = User(
            id = null,
            username = command.username,
            email = command.email,
            passwordHash = hashedPassword
        )

        val savedUser = userRepository.save(user)

        ratingRepository.findAllWithoutUser().forEach { rating ->
            ratingRepository.save(rating.copy(userId = savedUser.id!!))
        }
    }
}
