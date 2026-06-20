package org.ratelog.user.community

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
    val userId: User.Id,
    val limit: Int,
)

data class FeedItem(
    val username: String,
    val title: String,
    val tmdbId: Int,
    val type: String,
    val seasonNumber: Int?,
    val score: Double,
    val reviewText: String?,
    val ratedAt: String,
    val createdAtEpochMs: Long,
)

@Service
class CommunityHandler(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {

    @Transactional
    fun handle(query: FeedQuery): Either<CommunityHandlerError, List<FeedItem>> = either {
        val followedIds = userRepository.findFollowedUserIds(query.userId)
        if (followedIds.isEmpty()) return@either emptyList()

        val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS)

        val movieItems = ratingRepository.findFeedItemsByUserIdsAndLastDays(followedIds, thirtyDaysAgo, query.limit)
            .map { row ->
                FeedItem(
                    username = row.username,
                    title = row.title,
                    tmdbId = row.tmdbId,
                    type = MediaType.movie.name,
                    seasonNumber = null,
                    score = row.score ?: 0.0,
                    reviewText = row.reviewText,
                    ratedAt = row.createdAtEpochMs.toDateString(),
                    createdAtEpochMs = row.createdAtEpochMs,
                )
            }

        val tvItems = tvRatingRepository.findFeedItemsByUserIdsAndLastDays(followedIds, thirtyDaysAgo, query.limit)
            .map { row ->
                FeedItem(
                    username = row.username,
                    title = row.title,
                    tmdbId = row.tmdbId,
                    type = MediaType.tvshow.name,
                    seasonNumber = row.seasonNumber,
                    score = row.score ?: 0.0,
                    reviewText = row.reviewText,
                    ratedAt = row.createdAtEpochMs.toDateString(),
                    createdAtEpochMs = row.createdAtEpochMs,
                )
            }

        (movieItems + tvItems).sortedByDescending { it.createdAtEpochMs }
    }
}
