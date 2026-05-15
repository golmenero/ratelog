package org.raterr.profile

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.UserId
import org.raterr.friendship.Friendship
import org.raterr.friendship.FriendshipRepository
import org.raterr.premieres.ListPremiere
import org.raterr.premieres.ListPremiereHandler
import org.raterr.premieres.Premieres
import org.raterr.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

data class GetProfile(val userId: UserId)

data class Profile(
    val username: String,
    val email: String,
    val memberSince: String,
    val premieres: Premieres,
    val friends: List<Friendship>,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
    private val premiereHandler: ListPremiereHandler,
    private val friendshipRepository: FriendshipRepository,
) {

    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val user = userRepository.findById(query.userId.value)
            .getOrNull() ?: raise(ProfileHandlerError.UserNotFound)

        val premieres = premiereHandler.handle(ListPremiere(query.userId))
            .fold(
                { Premieres(emptyList(), emptyList(), emptyList()) },
                { it }
            )

        val friends = friendshipRepository.findAllFriendsByUserId(query.userId.value)

        Profile(
            username = user.username,
            email = user.email,
            memberSince = LocalDate.ofEpochDay(user.createdAtEpochMs / 86400000).toString(),
            premieres = premieres,
            friends = friends,
        )
    }
}
