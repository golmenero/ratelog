package org.ratelog.test

import org.ratelog.Rank
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.rating.FeedTvRow
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryTvRatingRepository : TvRatingRepository {
    private val store = ConcurrentHashMap<TvRating.Id, TvRating>()
    private val idGenerator = AtomicLong(1)

    override fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): TvRating? =
        store.values.find { it.tvShowId == tvShowId && it.userId == userId }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<Pair<Rank, TvRating>> =
        store.values
            .filter { it.userId == userId }
            .sortedByDescending { it.score?.value ?: 0.0 }
            .take(limit)
            .mapIndexed { index, rating -> Pair(Rank(index + 1), rating) }

    override fun findFeedItemsByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<FeedTvRow> =
        store.values
            .filter { it.userId in userIds && it.createdAt >= since }
            .map { FeedTvRow("user", "tvshow", 0, 1, it.score?.value, null, it.createdAt.toEpochMilli()) }

    override fun save(rating: TvRating) {
        val ratingToSave = if (rating.id == null) {
            rating.copy(id = TvRating.Id(idGenerator.getAndIncrement()))
        } else {
            rating
        }
        store[ratingToSave.id!!] = ratingToSave
    }

    override fun deleteById(tvRatingId: TvRating.Id) {
        store.remove(tvRatingId)
    }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
