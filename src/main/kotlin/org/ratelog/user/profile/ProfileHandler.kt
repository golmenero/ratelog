package org.ratelog.user.profile

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Username
import org.ratelog.formatMs
import org.ratelog.movie.rating.FeedMovieRow
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tvshow.rating.FeedTvRow
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
    val ratings: List<ProfileRating>,
)

data class ProfileRating(
    val title: String,
    val score: Double,
    val ratedAt: String,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)

        val user = userRepository.findById(query.userId) ?: raise(ProfileHandlerError.UserNotFound)
        val ratings = ratingRepository.findFeedItemsByUserIdsAndLastDays(listOf(query.userId), thirtyDaysAgo).map { toResponse(it) }
        val tvRatings = tvRatingRepository.findFeedItemsByUserIdsAndLastDays(listOf(query.userId), thirtyDaysAgo).map { toResponse(it) }

        Profile(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            memberSince = LocalDate.ofEpochDay(user.createdAtEpochMs / 86400000).toString(),
            lang = user.lang,
            isFollowed = user.followed,
            ratings =  (ratings + tvRatings).sortedByDescending { it.ratedAt },
        )
    }

    private fun toResponse(rating: FeedMovieRow) = ProfileRating(
        title = rating.title,
        score = rating.score!!,
        ratedAt = rating.createdAtEpochMs.formatMs(),
    )

    private fun toResponse(rating: FeedTvRow) = ProfileRating(
        title = rating.title,
        score = rating.score!!,
        ratedAt = rating.createdAtEpochMs.formatMs(),
    )
}
