package org.ratelog.user.profile

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

data class GetProfile(
    val loggedUserId: User.Id,
    val userId: User.Id,
)

data class Profile(
    val userId: User.Id,
    val username: Username,
    val email: Email,
    val memberSince: String,
    val lang: Lang,
    val isFollowed: Boolean,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
) {

    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val user = userRepository.findById(query.userId) ?: raise(ProfileHandlerError.UserNotFound)

        Profile(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            memberSince = LocalDate.ofEpochDay(user.createdAtEpochMs / 86400000).toString(),
            lang = user.lang,
            isFollowed = user.followed,
        )
    }
}
