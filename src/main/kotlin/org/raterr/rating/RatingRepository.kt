package org.raterr.rating

import org.raterr.Score
import org.raterr.Username
import org.raterr.movie.Movie
import org.raterr.user.User
import org.springframework.stereotype.Repository
import java.time.Instant

data class Rating(
    val id: Id?,
    val movieId: Movie.Id,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
    val rank: Rank,
) {
    data class Id(val value: Long)
    data class Rank(val value: Int)
}

data class RatingWithUsername(
    val id: Rating.Id,
    val movieId: Movie.Id,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
    val username: Username
)

@Repository
interface RatingRepository {
    fun findById(id: Rating.Id): Rating?
    fun findFirstByMovieId(movieId: Movie.Id): Rating?
    fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): List<Rating>
    fun findByUserId(userId: User.Id): List<Rating>
    fun findAllWithoutUser(): List<Rating>
    fun save(rating: Rating): Rating
    fun deleteByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Int
    fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<Rating>
    fun findByUserIdOrderedByRank(userId: User.Id): List<Rating>
    fun updateRank(id: Rating.Id, rank: Rating.Rank): Int
    fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<RatingWithUsername>
}
