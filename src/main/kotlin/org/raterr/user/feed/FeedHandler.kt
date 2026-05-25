package org.raterr.user.feed

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.userfollow.UserFollowRepository
import org.raterr.rating.RatingRepository
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvrating.TvRatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class FeedQuery(
    val userId: User.Id
)

data class FeedItem(
    val username: String,
    val title: String,
    val posterPath: String?,
    val tmdbId: Int,
    val type: String,
    val score: Double,
    val ratedAt: String,
    val createdAtEpochMs: Long
)

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault())

@Service
class FeedHandler(
    private val userFollowRepository: UserFollowRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    fun handle(query: FeedQuery): Either<FeedHandlerError, List<FeedItem>> = either {
        val followedIds = userFollowRepository.findFollowedUserIds(query.userId.value)
        if (followedIds.isEmpty()) return@either emptyList()

        val thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60)

        val movieRatings = ratingRepository.findByUserIdsAndLastDays(followedIds.map { User.Id(it) }, thirtyDaysAgo)
        val tvRatings = tvRatingRepository.findByUserIdsAndLastDays(followedIds.map { User.Id(it) }, thirtyDaysAgo)

        val movieItems = movieRatings.map { rating ->
            FeedItem(
                username = rating.user.username.value,
                title = rating.movie.title.value,
                posterPath = rating.movie.posterPath?.value,
                tmdbId = rating.movie.tmdbId.value,
                type = MediaType.movie.name,
                score = rating.score,
                ratedAt = dateFormatter.format(rating.createdAt),
                createdAtEpochMs = rating.createdAt.toEpochMilli()
            )
        }

        val tvItems = tvRatings.map { rating ->
            FeedItem(
                username = rating.user.username.value,
                title = rating.tvShow.name.value,
                posterPath = rating.tvShow.posterPath?.value,
                tmdbId = rating.tvShow.tmdbId.value,
                type = MediaType.tvshow.name,
                score = rating.score,
                ratedAt = dateFormatter.format(rating.createdAt),
                createdAtEpochMs = rating.createdAt.toEpochMilli()
            )
        }

        (movieItems + tvItems).sortedByDescending { it.createdAtEpochMs }
    }
}
