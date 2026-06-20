package org.ratelog.test

import org.ratelog.Rank
import org.ratelog.movie.Movie
import org.ratelog.movie.rating.FeedMovieRow
import org.ratelog.movie.rating.Rating
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryRatingRepository(
    val userRepository: UserRepository = InMemoryUserRepository()
) : RatingRepository {
    private val store = ConcurrentHashMap<Rating.Id, Rating>()
    private val idGenerator = AtomicLong(1)

    override fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Rating? =
        store.values.find { it.movieId == movieId && it.userId == userId }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<Pair<Rank, Rating>> =
        store.values
            .filter { it.userId == userId }
            .sortedByDescending { it.score?.value ?: 0.0 }
            .take(limit)
            .mapIndexed { index, rating -> Pair(Rank(index + 1), rating) }

    override fun findFeedItemsByUserIds(userIds: List<User.Id>, limit: Int): List<FeedMovieRow> =
        store.values
            .filter { it.userId in userIds }
            .sortedByDescending { it.createdAt.toEpochMilli() }
            .take(limit)
            .map {
                val user = userRepository.findById(it.userId)!!
                FeedMovieRow(user.username.value, "movie", 0, it.score?.value, it.review?.value, it.createdAt.toEpochMilli())
            }

    override fun countFeedItemsByUserIds(userIds: List<User.Id>): Long =
        store.values
            .count { it.userId in userIds }
            .toLong()

    override fun save(rating: Rating) {
        val ratingToSave = if (rating.id == null) {
            rating.copy(id = Rating.Id(idGenerator.getAndIncrement()))
        } else {
            rating
        }
        store[ratingToSave.id!!] = ratingToSave
    }

    override fun deleteById(ratingId: Rating.Id) {
        store.remove(ratingId)
    }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
