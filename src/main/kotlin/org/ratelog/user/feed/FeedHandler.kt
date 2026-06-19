package org.ratelog.user.feed

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.MediaType
import org.ratelog.toDateString
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.transaction.annotation.Transactional

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
    val createdAtEpochMs: Long,
)

@Service
class FeedHandler(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    @Transactional
    fun handle(query: FeedQuery): Either<FeedHandlerError, List<FeedItem>> = either {
        val followedIds = userRepository.findFollowedUserIds(query.userId)
        if (followedIds.isEmpty()) return@either emptyList()

        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)

        val movieItems = ratingRepository.findFeedItemsByUserIdsAndLastDays(followedIds, thirtyDaysAgo)
            .map { row ->
                FeedItem(
                    username = row.username,
                    title = row.title,
                    posterPath = row.posterPath,
                    tmdbId = row.tmdbId,
                    type = MediaType.movie.name,
                    score = row.score ?: 0.0,
                    ratedAt = row.createdAtEpochMs.toDateString(),
                    createdAtEpochMs = row.createdAtEpochMs,
                )
            }

        val tvItems = tvRatingRepository.findFeedItemsByUserIdsAndLastDays(followedIds, thirtyDaysAgo)
            .map { row ->
                FeedItem(
                    username = row.username,
                    title = row.title,
                    posterPath = row.posterPath,
                    tmdbId = row.tmdbId,
                    type = MediaType.tvshow.name,
                    score = row.score ?: 0.0,
                    ratedAt = row.createdAtEpochMs.toDateString(),
                    createdAtEpochMs = row.createdAtEpochMs,
                )
            }

        (movieItems + tvItems).sortedByDescending { it.createdAtEpochMs }
    }
}
