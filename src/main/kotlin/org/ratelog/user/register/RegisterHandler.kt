package org.ratelog.user.register

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class RegisterUser(
    val username: Username,
    val email: Email,
    val password: Password,
    val lang: Lang,
)

@Component
class RegisterHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun handle(command: RegisterUser): Either<RegisterHandlerError, Unit> = either {
        ensure(userRepository.findByUsername(command.username) == null) { RegisterHandlerError.UsernameAlreadyExists }
        ensure(userRepository.findByEmail(command.email) == null) { RegisterHandlerError.EmailAlreadyExists }

        val hashedPassword = command.password.value.let(passwordEncoder::encode)

        User(
            id = null,
            username = command.username,
            email = command.email,
            passwordHash = hashedPassword,
            lang = command.lang,
            metadataLang = command.lang,
        ).let(userRepository::save)
    }
}
