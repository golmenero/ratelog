package org.ratelog.movie.rating

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

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
    @Column("review_text") val reviewText: String?,
)

data class RatedMovieRow(
    val id: Long?,
    val movieId: Long,
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val score: Double?,
    val reviewText: String?,
    val rank: Long,
)

@Repository
interface RatingDAO : CrudRepository<RatingEntity, Long> {
    fun findFirstByMovieIdAndUserId(movieId: Long, userId: Long): RatingEntity?

    @Query(
        """
        WITH ranked AS (
            SELECT r.id, r.movie_id, r.user_id, r.directing, r.cinematography, r.acting,
                   r.soundtrack, r.screenplay, r.created_at_epoch_ms, r.score, r.review_text,
                   ROW_NUMBER() OVER (ORDER BY r.score DESC) AS rank
            FROM movie_ratings r
            WHERE r.user_id = :userId
        )
        SELECT ranked.*
        FROM ranked
        INNER JOIN movies m ON ranked.movie_id = m.id
        WHERE (:genreId IS NULL OR m.genres LIKE CONCAT('%', :genreId, '%'))
          AND (:name IS NULL OR LOWER(m.original_title) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY ranked.score DESC
        LIMIT :limit
        """
    )
    fun findRankedRows(userId: Long, genreId: String?, name: String?, limit: Int): List<RatedMovieRow>
}