package org.ratelog.user.create

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class CreateUserCommand(
    val currentUser: User,
    val username: Username,
    val email: Email,
    val password: Password,
    val lang: Lang,
    val role: Role,
)

@Component
class CreateUserHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun handle(command: CreateUserCommand): Either<CreateUserHandlerError, Unit> = either {
        ensure(command.currentUser.role.isAdminLike) { CreateUserHandlerError.Forbidden }
        ensure(command.role != Role.SUPERADMIN) { CreateUserHandlerError.CannotPromoteToSuperadmin }
        ensure(!(command.currentUser.role == Role.ADMIN && command.role == Role.ADMIN)) {
            CreateUserHandlerError.CannotCreateAdmin
        }
        ensure(userRepository.findByUsername(command.username) == null) {
            CreateUserHandlerError.UsernameAlreadyExists
        }
        ensure(userRepository.findByEmail(command.email) == null) {
            CreateUserHandlerError.EmailAlreadyExists
        }

        val hashedPassword = command.password.value.let(passwordEncoder::encode)

        User(
            id = null,
            username = command.username,
            email = command.email,
            passwordHash = hashedPassword,
            lang = command.lang,
            metadataLang = command.lang,
            role = command.role,
        ).let(userRepository::save)
    }
}