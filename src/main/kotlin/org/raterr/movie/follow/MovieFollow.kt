package org.raterr.movie.follow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("movie_follows")
data class MovieFollow(
    @Id val id: Long? = null,
    @Column("user_id") val userId: Long,
    @Column("movie_id") val movieId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)
