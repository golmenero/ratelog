package org.raterr.user.feed

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.userfollow.UserFollowRepository
import org.raterr.movie.MovieRepository
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.rating.RatingScoreService
import org.raterr.tvrating.TvRating
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvrating.TvRatingScoreService
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
    private val movieRepository: MovieRepository,
    private val tvShowRepository: TvShowRepository,
) {

    fun handle(query: FeedQuery): Either<FeedHandlerError, List<FeedItem>> = either {
        val followedIds = userFollowRepository.findFollowedUserIds(query.userId.value)
        if (followedIds.isEmpty()) return@either emptyList()

        val thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60)

        val movieRatings = ratingRepository.findByUserIdsAndLastDays(followedIds.map { User.Id(it) }, thirtyDaysAgo)
        val tvRatings = tvRatingRepository.findByUserIdsAndLastDays(followedIds.map { User.Id(it) }, thirtyDaysAgo)

        val movieItems = movieRatings.mapNotNull { rating ->
            val movie = rating.movieId.let(movieRepository::findById)
                ?: return@mapNotNull null
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
                    createdAt = rating.createdAt,
                    rank = Rating.Rank(0)
                )
            )
            FeedItem(
                username = rating.username.value,
                title = movie.title.value,
                posterPath = movie.posterPath?.value,
                tmdbId = movie.tmdbId.value,
                type = MediaType.movie.name,
                score = score,
                ratedAt = dateFormatter.format(rating.createdAt),
                createdAtEpochMs = rating.createdAt.toEpochMilli()
            )
        }

        val tvItems = tvRatings.mapNotNull { rating ->
            val show = rating.tvShowId.let(tvShowRepository::findById) ?: return@mapNotNull null
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
                    createdAt = rating.createdAt,
                    rank = TvRating.Rank(0)
                )
            )
            FeedItem(
                username = rating.username.value,
                title = show.name.value,
                posterPath = show.posterPath?.value,
                tmdbId = show.tmdbId.value,
                type = MediaType.tvshow.name,
                score = score,
                ratedAt = dateFormatter.format(rating.createdAt),
                createdAtEpochMs = rating.createdAt.toEpochMilli()
            )
        }

        (movieItems + tvItems).sortedByDescending { it.createdAtEpochMs }
    }
}
