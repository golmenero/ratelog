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
          AND (:year IS NULL OR t.first_air_year = :year)
          AND (:category IS NULL OR LOWER(t.genres) LIKE '%' || LOWER(:category) || '%')
        """
    )
    fun findByUserIdWithFilters(
        userId: Long,
        year: Int?,
        category: String?
    ): List<TvRating>
}
