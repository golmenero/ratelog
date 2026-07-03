package org.ratelog.feed.community

import arrow.core.Either
import arrow.core.getOrElse
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
    val followedUsers: List<FollowedUserResult>,
    val feed: List<FeedItem>,
    val hasMore: Boolean,
)

data class FeedItem(
    val username: String,
    val title: String,
    val tmdbId: Int,
    val mediaType: String,
    val type: String,
    val seasonNumber: Int?,
    val score: Double,
    val reviewText: String?,
    val ratedAt: String,
    val createdAtEpochMs: Long,
)

@Service
class CommunityHandler(
    private val feedRepository: FeedRepository,
    private val followedUsersHandler: FollowedUsersHandler,
) {

    @Transactional
    fun handle(query: FeedQuery): Either<CommunityHandlerError, FeedResult> = either {
        val followedUsers = followedUsersHandler.handle(FollowedUsersQuery(query.userId)).getOrElse { emptyList() }
        if (followedUsers.isEmpty()) return@either FeedResult(emptyList(), emptyList(),false)

        val followedIds = followedUsers.map { it.id.let(User::Id) }
        val items = feedRepository.findAll(followedIds, query.limit)
            .map { row ->
                FeedItem(
                    username = row.username.value,
                    title = row.title.value,
                    tmdbId = row.tmdbId.value,
                    mediaType = row.mediaType.name,
                    type = row.mediaType.name,
                    seasonNumber = row.seasonNumber?.value,
                    score = row.score?.value ?: 0.0,
                    reviewText = row.text,
                    ratedAt = row.createdAtEpochMs.toDateString(),
                    createdAtEpochMs = row.createdAtEpochMs,
                )
            }

        val totalCount = feedRepository.count(followedIds)
        FeedResult(followedUsers, items, totalCount > query.limit)
    }
}
