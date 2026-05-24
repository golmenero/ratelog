package org.raterr.userfollow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("follows_users")
data class UserFollow(
    @Id val id: Long?,
    @Column("follower_id") val followerId: Long,
    @Column("followed_id") val followedId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)
