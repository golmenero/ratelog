package org.raterr.user.profile

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.UserId
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

data class GetProfile(val userId: UserId)

data class Profile(
    val username: String,
    val email: String,
    val memberSince: String,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
) {

    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val user = userRepository.findById(query.userId.value)
            .getOrNull() ?: raise(ProfileHandlerError.UserNotFound)

        Profile(
            username = user.username,
            email = user.email,
            memberSince = LocalDate.ofEpochDay(user.createdAtEpochMs / 86400000).toString(),
        )
    }
}
