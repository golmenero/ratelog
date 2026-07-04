package org.ratelog.feed.community

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.feed.FeedItem
import org.ratelog.feed.FeedRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class FeedQuery(
    val userId: User.Id,
    val limit: Int,
)

data class FeedResult(
    val followedUsers: List<User>,
    val feed: List<FeedItem>,
    val hasMore: Boolean,
)

@Service
class CommunityHandler(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun handle(query: FeedQuery): Either<CommunityHandlerError, FeedResult> = either {
        val followedUsers = userRepository.findFollowingByUserId(query.userId)
        if (followedUsers.isEmpty()) return@either FeedResult(emptyList(), emptyList(),false)

        val followedIds = followedUsers.mapNotNull { it.id }
        val items = feedRepository.findAll(followedIds, query.limit)

        val totalCount = feedRepository.count(followedIds)
        FeedResult(followedUsers, items, totalCount > query.limit)
    }
}
