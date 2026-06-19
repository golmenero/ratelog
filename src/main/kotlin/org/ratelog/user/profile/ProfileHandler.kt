package org.ratelog.user.profile

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.Username
import org.ratelog.toDateString
import org.ratelog.movie.rating.FeedMovieRow
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tvshow.rating.FeedTvRow
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.transaction.annotation.Transactional

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
    val posterPath: String?,
    val tmdbId: Int,
    val type: String,
    val score: Double,
    val ratedAt: String,
    val createdAtEpochMs: Long,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    @Transactional
    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)

        val user = userRepository.findById(query.userId) ?: raise(ProfileHandlerError.UserNotFound)
        val ratings = ratingRepository.findFeedItemsByUserIdsAndLastDays(listOf(query.userId), thirtyDaysAgo).map { toResponse(it) }
        val tvRatings = tvRatingRepository.findFeedItemsByUserIdsAndLastDays(listOf(query.userId), thirtyDaysAgo).map { toResponse(it) }

        Profile(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            memberSince = user.createdAtEpochMs.toDateString(),
            lang = user.lang,
            isFollowed = userRepository.isFollowing(query.loggedUserId, query.userId),
            ratings =  (ratings + tvRatings).sortedByDescending { it.ratedAt },
        )
    }

    private fun toResponse(rating: FeedMovieRow) = ProfileRating(
        title = rating.title,
        posterPath = rating.posterPath,
        tmdbId = rating.tmdbId,
        type = MediaType.movie.name,
        score = rating.score ?: 0.0,
        ratedAt = rating.createdAtEpochMs.toDateString(),
        createdAtEpochMs = rating.createdAtEpochMs,
    )

    private fun toResponse(rating: FeedTvRow) = ProfileRating(
        title = rating.title,
        posterPath = rating.posterPath,
        tmdbId = rating.tmdbId,
        type = MediaType.tvshow.name,
        score = rating.score ?: 0.0,
        ratedAt = rating.createdAtEpochMs.toDateString(),
        createdAtEpochMs = rating.createdAtEpochMs,
    )
}
