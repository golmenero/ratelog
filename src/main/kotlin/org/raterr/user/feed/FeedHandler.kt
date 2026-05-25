package org.raterr.user.feed

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.movie.MovieRepository
import org.raterr.movie.rating.RatingRepository
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.user.User
import org.raterr.user.UserRepository
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
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val tvShowRepository: TvShowRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    fun handle(query: FeedQuery): Either<FeedHandlerError, List<FeedItem>> = either {
        val followedIds = userRepository.findFollowedUserIds(query.userId)
        if (followedIds.isEmpty()) return@either emptyList()

        val thirtyDaysAgo = Instant.now().minusSeconds(30L * 24 * 60 * 60)

        val movieRatings = ratingRepository.findByUserIdsAndLastDays(followedIds, thirtyDaysAgo)
        val tvRatings = tvRatingRepository.findByUserIdsAndLastDays(followedIds, thirtyDaysAgo)

        val movieItems = movieRatings.map { rating ->
            val user = userRepository.findById(rating.userId)!!
            val movie = movieRepository.findById(rating.movieId)!!
            FeedItem(
                username = user.username.value,
                title = movie.title.value,
                posterPath = movie.posterPath?.value,
                tmdbId = movie.tmdbId.value,
                type = MediaType.movie.name,
                score = rating.score,
                ratedAt = dateFormatter.format(rating.createdAt),
                createdAtEpochMs = rating.createdAt.toEpochMilli()
            )
        }

        val tvItems = tvRatings.map { rating ->
            val user = userRepository.findById(rating.userId)!!
            val tvShow = tvShowRepository.findById(rating.tvShowId)!!
            FeedItem(
                username = user.username.value,
                title = tvShow.name.value,
                posterPath = tvShow.posterPath?.value,
                tmdbId = tvShow.tmdbId.value,
                type = MediaType.tvshow.name,
                score = rating.score,
                ratedAt = dateFormatter.format(rating.createdAt),
                createdAtEpochMs = rating.createdAt.toEpochMilli()
            )
        }

        (movieItems + tvItems).sortedByDescending { it.createdAtEpochMs }
    }
}
