package org.ratelog.feed.community

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.MediaType
import org.ratelog.feed.FeedRepository
import org.ratelog.toDateString
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class FeedQuery(
    val userId: User.Id,
    val limit: Int,
)

data class FeedResult(
    val feed: List<FeedItem>,
    val hasMore: Boolean,
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
    private val feedRepository: FeedRepository,
) {

    @Transactional
    fun handle(query: FeedQuery): Either<CommunityHandlerError, FeedResult> = either {
        val followedIds = userRepository.findFollowedUserIds(query.userId)
        if (followedIds.isEmpty()) return@either FeedResult(emptyList(), false)

        val items = feedRepository.findAll(followedIds, query.limit)
            .map { row ->
                FeedItem(
                    username = row.username.value,
                    title = row.title.value,
                    tmdbId = row.tmdbId.value,
                    type = row.mediaType.name,
                    seasonNumber = row.seasonNumber?.value,
                    score = row.score?.value ?: 0.0,
                    reviewText = row.text,
                    ratedAt = row.createdAtEpochMs.toDateString(),
                    createdAtEpochMs = row.createdAtEpochMs,
                )
            }

        val totalCount = feedRepository.count(followedIds)

        FeedResult(items, totalCount > query.limit)
    }
}
