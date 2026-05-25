package org.raterr.tvrating

import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class InMemoryTvRatingRepository(
    private val tvShowRepository: InMemoryTvShowRepository = InMemoryTvShowRepository()
) : TvRatingRepository {

    private val storage = mutableListOf<TvRating>()
    private val users = mutableMapOf<Long, String>()
    private val idGenerator = AtomicLong(1)

    fun addUser(id: Long, username: String) {
        users[id] = username
    }

    fun clear() {
        storage.clear()
        users.clear()
        idGenerator.set(1)
    }

    override fun findById(id: TvRating.Id): TvRating? =
        storage.firstOrNull { it.id == id }

    override fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRating? =
        storage.firstOrNull { it.tvShowId == tvShowId }

    override fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): List<TvRating> =
        storage.filter { it.tvShowId == tvShowId && it.userId == userId }

    override fun findByUserId(userId: User.Id): List<TvRating> =
        storage.filter { it.userId == userId }

    override fun findAllWithoutUser(): List<TvRating> =
        storage.filter { it.userId.value == 0L }

    override fun save(rating: TvRating): TvRating =
        if (rating.id == null) {
            val newRating = rating.copy(id = TvRating.Id(idGenerator.getAndIncrement()))
            storage.add(newRating)
            newRating
        } else {
            storage.removeIf { it.id == rating.id }
            storage.add(rating)
            rating
        }

    override fun deleteByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): Int {
        val before = storage.size
        storage.removeIf { it.tvShowId == tvShowId && it.userId == userId }
        return before - storage.size
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRating> {
        val all = storage.filter { it.userId == userId }
            .sortedByDescending { TvRatingScoreService.score(it) }
            .mapIndexed { index, rating -> rating.copy(rank = TvRating.Rank(index + 1)) }
        return all.filter { ranked ->
            val show = tvShowRepository.findById(ranked.tvShowId)
            (category == null || show?.genres?.any { it.name.lowercase() == category.lowercase() } == true) &&
                    (name == null || show?.name?.value?.lowercase()?.contains(name.lowercase()) == true)
        }.take(limit)
    }

    override fun findByUserIdOrderedByRank(userId: User.Id): List<TvRating> =
        storage.filter { it.userId == userId }
            .sortedBy { it.rank.value }

    override fun updateRank(id: TvRating.Id, rank: TvRating.Rank): Int {
        val idx = storage.indexOfFirst { it.id == id }
        if (idx >= 0) {
            storage[idx] = storage[idx].copy(rank = rank)
            return 1
        }
        return 0
    }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRatingWithUsername> {
        val userIdValues = userIds.map(User.Id::value)
        val sinceEpochMs = since.toEpochMilli()
        return storage
            .filter { it.userId.value in userIdValues && it.createdAt.toEpochMilli() >= sinceEpochMs }
            .sortedByDescending { it.createdAt.toEpochMilli() }
            .map {
                TvRatingWithUsername(
                    id = it.id!!,
                    tvShowId = it.tvShowId,
                    userId = it.userId,
                    directing = it.directing,
                    cinematography = it.cinematography,
                    acting = it.acting,
                    soundtrack = it.soundtrack,
                    screenplay = it.screenplay,
                    createdAt = it.createdAt,
                    username = org.raterr.Username(users[it.userId.value] ?: "")
                )
            }
    }
}
