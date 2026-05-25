package org.raterr.rating

import org.raterr.movie.InMemoryMovieRepository
import org.raterr.movie.Movie
import org.raterr.user.User
import java.time.Instant
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

    override fun findById(id: Rating.Id): Rating? =
        storage.firstOrNull { it.id == id }

    override fun findFirstByMovieId(movieId: Movie.Id): Rating? =
        storage.firstOrNull { it.movieId == movieId }

    override fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): List<Rating> =
        storage.filter { it.movieId == movieId && it.userId == userId }

    override fun findByUserId(userId: User.Id): List<Rating> =
        storage.filter { it.userId == userId }

    override fun findAllWithoutUser(): List<Rating> =
        storage.filter { it.userId.value == 0L }

    override fun save(rating: Rating): Rating =
        if (rating.id == null) {
            val newRating = rating.copy(id = Rating.Id(idGenerator.getAndIncrement()))
            storage.add(newRating)
            newRating
        } else {
            storage.removeIf { it.id == rating.id }
            storage.add(rating)
            rating
        }

    override fun deleteByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Int {
        val before = storage.size
        storage.removeIf { it.movieId == movieId && it.userId == userId }
        return before - storage.size
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<Rating> {
        val all = storage.filter { it.userId == userId }
            .sortedByDescending { RatingScoreService.score(it) }
            .mapIndexed { index, rating -> rating.copy(rank = Rating.Rank(index + 1)) }
        return all.filter { ranked ->
            val movie = movieRepository.findById(ranked.movieId)
            (category == null || movie?.genres?.any { it.name.lowercase() == category.lowercase() } == true) &&
                    (name == null || movie?.title?.value?.lowercase()?.contains(name.lowercase()) == true)
        }.take(limit)
    }

    override fun findByUserIdOrderedByRank(userId: User.Id): List<Rating> =
        storage.filter { it.userId == userId }
            .sortedBy { it.rank.value }

    override fun updateRank(id: Rating.Id, rank: Rating.Rank): Int {
        val idx = storage.indexOfFirst { it.id == id }
        if (idx >= 0) {
            storage[idx] = storage[idx].copy(rank = rank)
            return 1
        }
        return 0
    }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<RatingWithUsername> {
        val userIdValues = userIds.map(User.Id::value)
        val sinceEpochMs = since.toEpochMilli()
        return storage
            .filter { it.userId.value in userIdValues && it.createdAt.toEpochMilli() >= sinceEpochMs }
            .sortedByDescending { it.createdAt.toEpochMilli() }
            .map {
                RatingWithUsername(
                    id = it.id!!,
                    movieId = it.movieId,
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
