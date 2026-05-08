package org.raterr.rating

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Long> {
    fun findByMovieId(movieId: Long): List<Rating>
    fun findByMovieIdAndUserId(movieId: Long, userId: Long): List<Rating>
    fun findByUserId(userId: Long): List<Rating>
    fun findAllWithoutUser(): List<Rating>

    @Modifying
    @Query("DELETE FROM ratings WHERE movie_id = :movieId AND user_id = :userId")
    fun deleteByMovieIdAndUserId(movieId: Long, userId: Long): Int
}
