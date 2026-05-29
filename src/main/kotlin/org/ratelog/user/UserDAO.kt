package org.ratelog.user

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("users")
data class UserEntity(
    @Id val id: Long? = null,
    val username: String,
    val email: String,
    @Column("password_hash") val passwordHash: String,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Table("users_follows")
data class UserFollowEntity(
    @Id val id: Long? = null,
    @Column("follower_id") val followerId: Long,
    @Column("followed_id") val followedId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Repository
interface UserDAO : CrudRepository<UserEntity, Long> {
    fun findByUsername(username: String): Optional<UserEntity>
    fun findByEmail(email: String): Optional<UserEntity>

    @Query("SELECT * FROM users WHERE username LIKE CONCAT('%', :username, '%')")
    fun findByUsernameContaining(username: String): List<UserEntity>
}

@Repository
interface UserFollowDAO : CrudRepository<UserFollowEntity, Long> {
    fun findByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Optional<UserFollowEntity>

    @Query("SELECT fu.followed_id FROM users_follows fu WHERE fu.follower_id = :userId")
    fun findFollowedUserIds(userId: Long): List<Long>

    @Query("SELECT fu.followed_id FROM users_follows fu WHERE fu.follower_id = :userId ORDER BY fu.created_at_epoch_ms DESC")
    fun findFollowingUserIds(userId: Long): List<Long>
}
