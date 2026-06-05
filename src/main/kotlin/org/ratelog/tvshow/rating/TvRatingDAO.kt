package org.ratelog.tvshow.rating

import org.ratelog.movie.rating.RatingEntity
import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("tv_ratings")
data class TvRatingEntity(
    @Id val id: Long? = null,
    @Column("tv_show_id") val tvShowId: Long,
    @Column("user_id") val userId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
    val score: Double?,
)

@Table("season_ratings")
data class SeasonRatingEntity(
    @Id val id: Long? = null,
    @Column("tv_show_id") val tvShowId: Long,
    @Column("season_number") val seasonNumber: Int,
    @Column("user_id") val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
)

@Repository
interface TvRatingDAO : CrudRepository<TvRatingEntity, Long> {
    fun findFirstByTvShowIdAndUserId(tvShowId: Long, userId: Long): Optional<TvRatingEntity>

    @Query("SELECT r.id FROM tv_ratings r WHERE r.user_id = :userId ORDER BY r.score DESC")
    fun findRanking(userId: Long): List<Long>

    @Query(
        """
        SELECT r.* FROM tv_ratings r
            INNER JOIN tv t ON r.tv_show_id = t.id
        WHERE r.user_id = :userId
            AND (:category IS NULL OR t.genres LIKE CONCAT('%', :category, '%'))
            AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY r.score DESC
        LIMIT :limit
        """
    )
    fun findRankedByUserIdWithFilters(userId: Long, category: String?, name: String?, limit: Int): List<TvRatingEntity>

    @Query(
        """
        SELECT r.* FROM tv_ratings r
        WHERE r.user_id IN (:userIds) AND r.created_at_epoch_ms >= :sinceEpochMs
        ORDER BY r.created_at_epoch_ms DESC
        """
    )
    fun findByUserIdsAndSince(userIds: List<Long>, sinceEpochMs: Long): List<TvRatingEntity>
}

@Repository
interface SeasonRatingDAO : CrudRepository<SeasonRatingEntity, Long> {
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<SeasonRatingEntity>
}