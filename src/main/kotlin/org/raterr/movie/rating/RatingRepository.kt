package org.raterr.movie.rating

import org.raterr.Score
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
    val score: Score? = null,
) {
    data class Id(val value: Long)

    fun updateScore() = copy(score = Score((directing.value + cinematography.value + acting.value + soundtrack.value + screenplay.value) / 5.0))
}

@Repository
interface RatingRepository {
    fun findById(id: Rating.Id): Rating?
    fun findFirstByMovieId(movieId: Movie.Id): Rating?
    fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): List<Rating>
    fun findByUserId(userId: User.Id): List<Rating>
    fun findAllWithoutUser(): List<Rating>
    fun findRankedByUserIdWithFilters(userId: User.Id, category: String?, limit: Int, name: String?): List<Rating>
    fun findByUserIdOrderedByRank(userId: User.Id): List<Rating>
    fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<Rating>

    fun save(rating: Rating)
    fun deleteByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Int
}
