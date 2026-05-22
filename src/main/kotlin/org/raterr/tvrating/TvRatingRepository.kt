package org.raterr.tvrating

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TvRatingRepository : CrudRepository<TvRating, Long> {
    fun findFirstByTvShowId(tvShowId: Long): TvRating?
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<TvRating>
    fun findByUserId(userId: Long): List<TvRating>

    @Modifying
    @Query("DELETE FROM tv_ratings WHERE tv_show_id = :tvShowId AND user_id = :userId")
    fun deleteByTvShowIdAndUserId(tvShowId: Long, userId: Long): Int

    @Query(
        """
        WITH ranked AS (
            SELECT r.*,
                   ROW_NUMBER() OVER (ORDER BY (r.directing + r.cinematography + r.acting + r.soundtrack + r.screenplay) DESC) as abs_rank
            FROM tv_ratings r
            WHERE r.user_id = :userId
        )
        SELECT r.id, r.tv_show_id, r.user_id, r.directing, r.cinematography, r.acting, r.soundtrack, r.screenplay, r.created_at_epoch_ms, r.abs_rank
        FROM ranked r
        JOIN tv_shows t ON r.tv_show_id = t.id
        WHERE (:category IS NULL OR LOWER(t.genres) LIKE '%' || LOWER(:category) || '%')
          AND (:name IS NULL OR LOWER(t.name) LIKE '%' || LOWER(:name) || '%')
        ORDER BY r.abs_rank
        LIMIT :limit
        """
    )
    fun findRankedByUserIdWithFilters(
        userId: Long,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRatingWithRank>

    @Query(
        """
        SELECT r.*, u.username AS username FROM tv_ratings r
        JOIN users u ON u.id = r.user_id
        WHERE r.user_id IN (:userIds)
          AND r.created_at_epoch_ms >= :sinceEpochMs
        ORDER BY r.created_at_epoch_ms DESC
        """
    )
    fun findByUserIdsAndLastDays(userIds: List<Long>, sinceEpochMs: Long): List<TvRatingWithUsername>
}

data class TvRatingWithRank(
    val id: Long? = null,
    val tvShowId: Long,
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val absRank: Int
)

data class TvRatingWithUsername(
    val id: Long? = null,
    val tvShowId: Long,
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val username: String
)
