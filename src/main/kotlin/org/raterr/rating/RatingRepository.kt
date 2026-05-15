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
          AND (:year IS NULL OR m.release_year = :year)
          AND (:category IS NULL OR LOWER(m.genres) LIKE '%' || LOWER(:category) || '%')
          AND (:name IS NULL OR LOWER(m.title) LIKE '%' || LOWER(:name) || '%')
        ORDER BY (r.directing + r.cinematography + r.acting + r.soundtrack + r.screenplay) DESC
        LIMIT :limit
        """
    )
    fun findByUserIdWithFilters(
        userId: Long,
        year: Int?,
        category: String?,
        limit: Int,
        name: String?
    ): List<Rating>
}
