package org.ratelog.tvshow.rating

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
    val score: Double,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
    @Column("review_text") val reviewText: String? = null,
)

@Repository
interface TvRatingDAO : CrudRepository<TvRatingEntity, Long> {
    fun findFirstByTvShowIdAndUserId(tvShowId: Long, userId: Long): Optional<TvRatingEntity>

    @Query(
        """
        WITH ranked AS (
            SELECT r.id, r.tv_show_id, r.user_id, r.created_at_epoch_ms, r.score,
                   ROW_NUMBER() OVER (ORDER BY r.score DESC) AS rank
            FROM tv_ratings r
            WHERE r.user_id = :userId
        )
        SELECT ranked.*
        FROM ranked
        INNER JOIN tv t ON ranked.tv_show_id = t.id
        WHERE (:category IS NULL OR t.genres LIKE CONCAT('%', :category, '%'))
          AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY ranked.score DESC
        LIMIT :limit
        """
    )
    fun findRankedRows(userId: Long, category: String?, name: String?, limit: Int): List<RatedTvRow>

    @Query(
        """
        SELECT COUNT(*)
        FROM season_ratings r
        WHERE r.user_id IN (:userIds)
        """
    )
    fun countFeedItemsByUserIds(userIds: List<Long>): Long

    @Query(
        """
        SELECT u.username, t.name AS title, t.tmdb_id, r.season_number, r.score, r.review_text, r.created_at_epoch_ms
        FROM season_ratings r
        INNER JOIN tv t ON r.tv_show_id = t.id
        INNER JOIN users u ON r.user_id = u.id
        WHERE r.user_id IN (:userIds)
        ORDER BY r.created_at_epoch_ms DESC
        LIMIT :limit
        """
    )
    fun findFeedItemsByUserIds(userIds: List<Long>, limit: Int): List<FeedTvRow>
}

data class FeedTvRow(
    val username: String,
    val title: String,
    val tmdbId: Int,
    val seasonNumber: Int?,
    val score: Double?,
    val reviewText: String?,
    val createdAtEpochMs: Long,
)

data class RatedTvRow(
    val id: Long?,
    val tvShowId: Long,
    val userId: Long,
    val createdAtEpochMs: Long,
    val score: Double?,
    val rank: Long,
)

@Repository
interface SeasonRatingDAO : CrudRepository<SeasonRatingEntity, Long> {
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<SeasonRatingEntity>
}