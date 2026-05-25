package org.raterr.tvshow.follow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tv_follows")
data class TvFollow(
    @Id val id: Long? = null,
    @Column("user_id") val userId: Long,
    @Column("tv_show_id") val tvShowId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)
