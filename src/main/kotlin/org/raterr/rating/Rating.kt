package org.raterr.rating

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("ratings")
data class Rating(
    @Id
    val id: Long? = null,
    @Column("movie_id")
    val movieId: Long,
    @Column("user_id")
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    @Column("created_at_epoch_ms")
    val createdAtEpochMs: Long,
    val rank: Int,
)
