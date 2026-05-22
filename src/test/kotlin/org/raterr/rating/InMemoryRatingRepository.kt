package org.raterr.rating

import org.raterr.movie.InMemoryMovieRepository
import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryRatingRepository(
    private val movieRepository: InMemoryMovieRepository = InMemoryMovieRepository()
) : RatingRepository {

    private val storage = mutableListOf<Rating>()
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

    override fun <S : Rating> save(entity: S): S {
        @Suppress("UNCHECKED_CAST")
        return if (entity.id == null) {
            val newEntity = entity.copy(id = idGenerator.getAndIncrement()) as S
            storage.add(newEntity)
            newEntity
        } else {
            storage.removeIf { it.id == entity.id }
            storage.add(entity)
            entity
        }
    }

    override fun <S : Rating> saveAll(entities: Iterable<S>): Iterable<S> = entities.map { save(it) }

    override fun findById(id: Long): Optional<Rating> =
        storage.find { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsById(id: Long): Boolean = storage.any { it.id == id }

    override fun findAll(): Iterable<Rating> = storage.toList()

    override fun findAllById(ids: Iterable<Long>): Iterable<Rating> = storage.filter { it.id in ids }

    override fun count(): Long = storage.size.toLong()

    override fun deleteById(id: Long) {
        storage.removeIf { it.id == id }
    }

    override fun delete(entity: Rating) {
        storage.removeIf { it.id == entity.id }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { deleteById(it) }
    }

    override fun deleteAll(entities: Iterable<Rating>) {
        entities.forEach { delete(it) }
    }

    override fun deleteAll() {
        storage.clear()
    }

    override fun findFirstByMovieId(movieId: Long): Rating? =
        storage.find { it.movieId == movieId }

    override fun findByMovieIdAndUserId(movieId: Long, userId: Long): List<Rating> =
        storage.filter { it.movieId == movieId && it.userId == userId }

    override fun findByUserId(userId: Long): List<Rating> =
        storage.filter { it.userId == userId }

    override fun findAllWithoutUser(): List<Rating> =
        storage.filter { it.userId == 0L }

    override fun deleteByMovieIdAndUserId(movieId: Long, userId: Long): Int {
        val before = storage.size
        storage.removeIf { it.movieId == movieId && it.userId == userId }
        return before - storage.size
    }

    override fun findRankedByUserIdWithFilters(
        userId: Long,
        category: String?,
        limit: Int,
        name: String?
    ): List<Rating> {
        val all = storage.filter { it.userId == userId }
            .sortedByDescending { it.directing + it.cinematography + it.acting + it.soundtrack + it.screenplay }
            .mapIndexed { index, rating -> rating.copy(rank = index + 1) }
        return all.filter { ranked ->
            val movie = movieRepository.findById(ranked.movieId).orElse(null)
            (category == null || movie?.genres?.lowercase()?.contains(category.lowercase()) == true) &&
            (name == null || movie?.title?.lowercase()?.contains(name.lowercase()) == true)
        }.take(limit)
    }

    override fun findByUserIdOrderedByRank(userId: Long): List<Rating> =
        storage.filter { it.userId == userId }
            .sortedBy { it.rank }

    override fun updateRank(id: Long, rank: Int): Int {
        val idx = storage.indexOfFirst { it.id == id }
        if (idx >= 0) {
            storage[idx] = storage[idx].copy(rank = rank)
            return 1
        }
        return 0
    }

    override fun findByUserIdsAndLastDays(userIds: List<Long>, sinceEpochMs: Long): List<RatingWithUsername> =
        storage
            .filter { it.userId in userIds && it.createdAtEpochMs >= sinceEpochMs }
            .sortedByDescending { it.createdAtEpochMs }
            .map {
                RatingWithUsername(
                    id = it.id,
                    movieId = it.movieId,
                    userId = it.userId,
                    directing = it.directing,
                    cinematography = it.cinematography,
                    acting = it.acting,
                    soundtrack = it.soundtrack,
                    screenplay = it.screenplay,
                    createdAtEpochMs = it.createdAtEpochMs,
                    username = users[it.userId] ?: ""
                )
            }
}
