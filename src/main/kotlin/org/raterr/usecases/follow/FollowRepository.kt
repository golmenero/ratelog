package org.raterr.usecases.follow

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FollowRepository : CrudRepository<Follow, Long> {
    fun findByUserIdAndContentTypeAndContentTmdbId(
        userId: Long,
        contentType: String,
        contentTmdbId: Int
    ): Optional<Follow>

    fun existsByUserIdAndContentTypeAndContentTmdbId(
        userId: Long,
        contentType: String,
        contentTmdbId: Int
    ): Boolean

    fun findByUserId(userId: Long): List<Follow>
}
