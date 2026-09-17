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
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
    val lang: String,
    @Column("metadata_lang") val metadataLang: String = "en",
    val role: String,
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

    @Query("SELECT * FROM users ORDER BY created_at_epoch_ms DESC, id DESC")
    fun findAllOrdered(): List<UserEntity>

    @org.springframework.data.jdbc.repository.query.Modifying
    @Query("DELETE FROM users WHERE id = :id")
    fun deleteByIdRaw(id: Long)

    @org.springframework.data.jdbc.repository.query.Modifying
    @Query("UPDATE users SET role = :role WHERE id = :id")
    fun updateRoleRaw(id: Long, role: String)

    @org.springframework.data.jdbc.repository.query.Modifying
    @Query("UPDATE users SET username = :username, email = :email, password_hash = :passwordHash WHERE id = :id")
    fun updateCredentialsRaw(id: Long, username: String, email: String, passwordHash: String)
}

@Repository
interface UserFollowDAO : CrudRepository<UserFollowEntity, Long> {
    fun findByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Optional<UserFollowEntity>

    @Query("SELECT u.* FROM users u INNER JOIN users_follows uf ON u.id = uf.followed_id WHERE uf.follower_id = :userId ORDER BY uf.created_at_epoch_ms DESC")
    fun findFollowingUsers(userId: Long): List<UserEntity>
}
