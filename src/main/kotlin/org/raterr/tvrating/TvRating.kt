package org.raterr.tvrating

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tv_ratings")
data class TvRating(
    @Id
    val id: Long? = null,
    @Column("tv_show_id")
    val tvShowId: Long,
    @Column("user_id")
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    @Column("created_at_epoch_ms")
    val createdAtEpochMs: Long,
    val rank: Int
)
