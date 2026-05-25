package org.raterr.tvshow.follow

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TvFollowRepository : CrudRepository<TvFollow, Long> {
    fun findByUserIdAndTvShowId(
        userId: Long,
        tvShowId: Long
    ): Optional<TvFollow>

    fun existsByUserIdAndTvShowId(
        userId: Long,
        tvShowId: Long
    ): Boolean

    fun findByUserId(userId: Long): List<TvFollow>
}
