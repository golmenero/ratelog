package org.ratelog.user.changemetadataLang

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class ChangeMetadataLangCommand(
    val userId: User.Id,
    val metadataLang: Lang,
)

@Component
class ChangeMetadataLangHandler(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun handle(command: ChangeMetadataLangCommand): Either<ChangeMetadataLangHandlerError, Unit> = either {
        val user = userRepository.findById(command.userId) ?: raise(ChangeMetadataLangHandlerError.UserNotFound)

        val updatedUser = user.copy(metadataLang = command.metadataLang)
        userRepository.save(updatedUser)
    }
}
