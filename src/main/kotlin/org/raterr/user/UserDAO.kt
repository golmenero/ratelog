package org.raterr.user

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

@Repository
interface UserDAO : CrudRepository<UserEntity, Long> {
    fun findByUsername(username: String): Optional<UserEntity>
    fun findByEmail(email: String): Optional<UserEntity>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean

    @Query("SELECT * FROM users WHERE username LIKE CONCAT('%', :username, '%')")
    fun findByUsernameContaining(username: String): List<UserEntity>
}
