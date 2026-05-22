package org.raterr.rating

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Long> {
    fun findFirstByMovieId(movieId: Long): Rating?
    fun findByMovieIdAndUserId(movieId: Long, userId: Long): List<Rating>
    fun findByUserId(userId: Long): List<Rating>

    @Query("SELECT * FROM ratings WHERE user_id IS NULL")
    fun findAllWithoutUser(): List<Rating>

    @Modifying
    @Query("DELETE FROM ratings WHERE movie_id = :movieId AND user_id = :userId")
    fun deleteByMovieIdAndUserId(movieId: Long, userId: Long): Int

    @Query(
        """
        SELECT r.* FROM ratings r
        JOIN movies m ON r.movie_id = m.id
        WHERE r.user_id = :userId
          AND (:category IS NULL OR LOWER(m.genres) LIKE '%' || LOWER(:category) || '%')
          AND (:name IS NULL OR LOWER(m.title) LIKE '%' || LOWER(:name) || '%')
        ORDER BY r.rank
        LIMIT :limit
        """
    )
    fun findRankedByUserIdWithFilters(
        userId: Long,
        category: String?,
        limit: Int,
        name: String?
    ): List<Rating>

    @Query(
        """
        SELECT r.*, u.username AS username FROM ratings r
        JOIN users u ON u.id = r.user_id
        WHERE r.user_id IN (:userIds)
          AND r.created_at_epoch_ms >= :sinceEpochMs
        ORDER BY r.created_at_epoch_ms DESC
        """
    )
    fun findByUserIdsAndLastDays(userIds: List<Long>, sinceEpochMs: Long): List<RatingWithUsername>
}

data class RatingWithUsername(
    val id: Long? = null,
    val movieId: Long,
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val username: String
)
