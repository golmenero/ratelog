package org.raterr.movie.follow

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MovieFollowRepository : CrudRepository<MovieFollow, Long> {
    fun findByUserIdAndMovieId(
        userId: Long,
        movieId: Long
    ): Optional<MovieFollow>

    fun existsByUserIdAndMovieId(
        userId: Long,
        movieId: Long
    ): Boolean

    fun findByUserId(userId: Long): List<MovieFollow>
}
