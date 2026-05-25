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

data class RatingView(
    val id: Rating.Id?,
    val movie: Movie,
    val user: User,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
    val rank: Rating.Rank,
) {
    val score = (directing.value + cinematography.value + acting.value + soundtrack.value + screenplay.value) / 5.0
}

@Repository
interface RatingRepository {
    fun findById(id: Rating.Id): RatingView?
    fun findFirstByMovieId(movieId: Movie.Id): RatingView?
    fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): List<RatingView>
    fun findByUserId(userId: User.Id): List<RatingView>
    fun findAllWithoutUser(): List<RatingView>
    fun findRankedByUserIdWithFilters(userId: User.Id, category: String?, limit: Int, name: String?): List<RatingView>
    fun findByUserIdOrderedByRank(userId: User.Id): List<RatingView>
    fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<RatingView>

    fun save(rating: Rating)
    fun deleteByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Int
    fun updateRank(id: Rating.Id, rank: Rating.Rank): Int
}
