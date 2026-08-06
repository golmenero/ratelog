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

data class RatedTvRow(
    val id: Long?,
    val tvShowId: Long,
    val userId: Long,
    val createdAtEpochMs: Long,
    val score: Double?,
    val rank: Long,
    val avgDirecting: Double?,
    val avgCinematography: Double?,
    val avgActing: Double?,
    val avgSoundtrack: Double?,
    val avgScreenplay: Double?,
    val avgScore: Double?,
)

@Repository
interface TvRatingDAO : CrudRepository<TvRatingEntity, Long> {
    fun findFirstByTvShowIdAndUserId(tvShowId: Long, userId: Long): Optional<TvRatingEntity>

    @Query(
        """
        WITH season_averages AS (
            SELECT sr.tv_show_id,
                   AVG(sr.directing) as avg_directing,
                   AVG(sr.cinematography) as avg_cinematography,
                   AVG(sr.acting) as avg_acting,
                   AVG(sr.soundtrack) as avg_soundtrack,
                   AVG(sr.screenplay) as avg_screenplay,
                   AVG(sr.score) as avg_score
            FROM season_ratings sr
            WHERE sr.user_id = :userId
            GROUP BY sr.tv_show_id
        ),
        ranked AS (
            SELECT r.id, r.tv_show_id, r.user_id, r.created_at_epoch_ms, r.score,
                   sa.avg_directing, sa.avg_cinematography, sa.avg_acting,
                   sa.avg_soundtrack, sa.avg_screenplay, sa.avg_score,
                   ROW_NUMBER() OVER (
                       ORDER BY CASE :ratingCategory
                           WHEN 'directing' THEN sa.avg_directing
                           WHEN 'cinematography' THEN sa.avg_cinematography
                           WHEN 'acting' THEN sa.avg_acting
                           WHEN 'soundtrack' THEN sa.avg_soundtrack
                           WHEN 'screenplay' THEN sa.avg_screenplay
                           ELSE r.score
                       END DESC
                   ) AS rank
            FROM tv_ratings r
            INNER JOIN season_averages sa ON r.tv_show_id = sa.tv_show_id
            WHERE r.user_id = :userId
        )
        SELECT ranked.*
        FROM ranked
        INNER JOIN tv t ON ranked.tv_show_id = t.id
        WHERE (:genreId IS NULL OR t.genres LIKE CONCAT('%', :genreId, '%'))
          AND (:name IS NULL OR LOWER(t.original_name) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY CASE :ratingCategory
            WHEN 'directing' THEN ranked.avg_directing
            WHEN 'cinematography' THEN ranked.avg_cinematography
            WHEN 'acting' THEN ranked.avg_acting
            WHEN 'soundtrack' THEN ranked.avg_soundtrack
            WHEN 'screenplay' THEN ranked.avg_screenplay
            ELSE ranked.score
        END DESC
        LIMIT :limit
        """
    )
    fun findRankedRows(userId: Long, genreId: String?, name: String?, limit: Int, ratingCategory: String?): List<RatedTvRow>
}

@Repository
interface SeasonRatingDAO : CrudRepository<SeasonRatingEntity, Long> {
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<SeasonRatingEntity>
}