package org.raterr.user.feed

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.UserId
import org.raterr.userfollow.UserFollowRepository
import org.raterr.movie.MovieRepository
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.rating.RatingScoreService
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvrating.TvRating
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvrating.TvRatingScoreService
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrNull

data class FeedQuery(
    val userId: UserId
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
    private val movieRepository: MovieRepository,
    private val tvShowRepository: TvShowRepository,
) {

    fun handle(query: FeedQuery): Either<FeedHandlerError, List<FeedItem>> = either {
        val followedIds = userFollowRepository.findFollowedUserIds(query.userId.value)
        if (followedIds.isEmpty()) return@either emptyList()

        val thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60).toEpochMilli()

        val movieRatings = ratingRepository.findByUserIdsAndLastDays(followedIds, thirtyDaysAgo)
        val tvRatings = tvRatingRepository.findByUserIdsAndLastDays(followedIds, thirtyDaysAgo)

        val movieItems = movieRatings.mapNotNull { rating ->
            val movie = movieRepository.findById(rating.movieId).getOrNull() ?: return@mapNotNull null
            val score = RatingScoreService.score(
                Rating(
                    id = rating.id,
                    movieId = rating.movieId,
                    userId = rating.userId,
                    directing = rating.directing,
                    cinematography = rating.cinematography,
                    acting = rating.acting,
                    soundtrack = rating.soundtrack,
                    screenplay = rating.screenplay,
                    createdAtEpochMs = rating.createdAtEpochMs
                )
            )
            FeedItem(
                username = rating.username,
                title = movie.title,
                posterPath = movie.posterPath,
                tmdbId = movie.tmdbId,
                type = MediaType.movie.name,
                score = score,
                ratedAt = dateFormatter.format(Instant.ofEpochMilli(rating.createdAtEpochMs)),
                createdAtEpochMs = rating.createdAtEpochMs
            )
        }

        val tvItems = tvRatings.mapNotNull { rating ->
            val show = tvShowRepository.findById(rating.tvShowId).getOrNull() ?: return@mapNotNull null
            val score = TvRatingScoreService.score(
                TvRating(
                    id = rating.id,
                    tvShowId = rating.tvShowId,
                    userId = rating.userId,
                    directing = rating.directing,
                    cinematography = rating.cinematography,
                    acting = rating.acting,
                    soundtrack = rating.soundtrack,
                    screenplay = rating.screenplay,
                    createdAtEpochMs = rating.createdAtEpochMs
                )
            )
            FeedItem(
                username = rating.username,
                title = show.name,
                posterPath = show.posterPath,
                tmdbId = show.tmdbId,
                type = MediaType.tvshow.name,
                score = score,
                ratedAt = dateFormatter.format(Instant.ofEpochMilli(rating.createdAtEpochMs)),
                createdAtEpochMs = rating.createdAtEpochMs
            )
        }

        (movieItems + tvItems).sortedByDescending { it.createdAtEpochMs }
    }
}
