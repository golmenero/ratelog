package org.ratelog.user.changelang

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Component

data class ChangeLangCommand(
    val userId: User.Id,
    val lang: Lang,
)

@Component
class ChangeLangHandler(
    private val userRepository: UserRepository,
) {
    fun handle(command: ChangeLangCommand): Either<ChangeLangHandlerError, Unit> = either {
        val user = userRepository.findById(command.userId) ?: raise(ChangeLangHandlerError.UserNotFound)

        val updatedUser = user.copy(lang = command.lang)
        userRepository.save(updatedUser)
    }
}
