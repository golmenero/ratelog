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
        SELECT r.* FROM tv_ratings r
        JOIN tv_shows t ON r.tv_show_id = t.id
        WHERE r.user_id = :userId
          AND (:category IS NULL OR LOWER(t.genres) LIKE '%' || LOWER(:category) || '%')
          AND (:name IS NULL OR LOWER(t.name) LIKE '%' || LOWER(:name) || '%')
        ORDER BY r.rank
        LIMIT :limit
        """
    )
    fun findRankedByUserIdWithFilters(
        userId: Long,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRating>

    @Query("SELECT r.* FROM tv_ratings r WHERE r.user_id = :userId ORDER BY r.rank")
    fun findByUserIdOrderedByRank(userId: Long): List<TvRating>

    @Modifying
    @Query("UPDATE tv_ratings SET rank = :rank WHERE id = :id")
    fun updateRank(id: Long, rank: Int): Int

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
