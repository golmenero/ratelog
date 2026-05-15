package org.raterr.friendship

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("friendships")
data class Friendship(
    @Id
    val id: Long? = null,
    @Column("user_id")
    val userId: Long,
    @Column("friend_id")
    val friendId: Long,
    val status: String,
    @Column("created_at_epoch_ms")
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
