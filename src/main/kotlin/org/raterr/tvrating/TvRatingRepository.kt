package org.raterr.tvrating

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TvRatingRepository : CrudRepository<TvRating, Long> {
    fun findByTvShowId(tvShowId: Long): List<TvRating>
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<TvRating>
    fun findByUserId(userId: Long): List<TvRating>

    @Modifying
    @Query("DELETE FROM tv_ratings WHERE tv_show_id = :tvShowId AND user_id = :userId")
    fun deleteByTvShowIdAndUserId(tvShowId: Long, userId: Long): Int
}
