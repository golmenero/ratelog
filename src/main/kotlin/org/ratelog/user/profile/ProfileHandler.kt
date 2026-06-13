package org.ratelog.user.profile

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Review
import org.ratelog.Username
import org.ratelog.movie.rating.Rating
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Instant

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
    val ratings: List<Rating>,
    val tvRatings: List<TvRating>,

    )

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val thirtyDaysAgo = java.time.Instant.now().minus(30, ChronoUnit.DAYS)

        val user = userRepository.findById(query.userId) ?: raise(ProfileHandlerError.UserNotFound)
        val ratings = ratingRepository.findByUserIdsAndLastDays(listOf(query.userId), thirtyDaysAgo)
        val tvRatings = tvRatingRepository.findByUserIdsAndLastDays(listOf(query.userId), thirtyDaysAgo)

        Profile(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            memberSince = LocalDate.ofEpochDay(user.createdAtEpochMs / 86400000).toString(),
            lang = user.lang,
            isFollowed = user.followed,
            ratings = ratings,
            tvRatings = tvRatings,
        )
    }
}
