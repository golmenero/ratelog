package org.ratelog.movie.rating

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("movie_ratings")
data class RatingEntity(
    @Id val id: Long? = null,
    @Column("movie_id") val movieId: Long,
    @Column("user_id") val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
    val score: Double?,
)

@Repository
interface RatingDAO : CrudRepository<RatingEntity, Long> {
    fun findFirstByMovieIdAndUserId(movieId: Long, userId: Long): RatingEntity?

    @Query("SELECT r.id FROM movie_ratings r WHERE r.user_id = :userId ORDER BY r.score DESC")
    fun findRanking(userId: Long): List<Long>

    @Query(
        """
        SELECT r.* FROM movie_ratings r
            INNER JOIN movies m ON r.movie_id = m.id
        WHERE r.user_id = :userId
            AND (:category IS NULL OR m.genres LIKE CONCAT('%', :category, '%'))
            AND (:name IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY r.score DESC
        LIMIT :limit
        """
    )
    fun findRankedByUserIdWithFilters(userId: Long, category: String?, name: String?, limit: Int): List<RatingEntity>
}
