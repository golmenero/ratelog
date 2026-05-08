package org.raterr.follow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("follows")
data class Follow(
    @Id
    val id: Long? = null,
    @Column("user_id")
    val userId: Long,
    @Column("content_type")
    val contentType: String,
    @Column("content_tmdb_id")
    val contentTmdbId: Int,
    @Column("created_at_epoch_ms")
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
