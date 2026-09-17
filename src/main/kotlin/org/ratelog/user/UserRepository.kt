package org.ratelog.user

import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Role
import org.ratelog.Username

data class User(
    val id: Id?,
    val username: Username,
    val email: Email,
    val passwordHash: String,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val lang: Lang,
    val metadataLang: Lang,
    val role: Role,
) {
    data class Id(val value: Long)
}

interface UserRepository {
    fun findById(id: User.Id): User?
    fun findByUsername(username: Username): User?
    fun findByEmail(email: Email): User?
    fun save(user: User): User
    fun findByUsernameContaining(username: Username): List<User>
    fun findByUsernameContaining(username: Username, followerId: User.Id): List<User>

    fun findFollowingByUserId(userId: User.Id): List<User>
    fun isFollowing(followerId: User.Id, followedId: User.Id): Boolean
    fun toggleFollow(followerId: User.Id, followedId: User.Id)

    fun findAll(): List<User>
    fun deleteById(id: User.Id)
    fun updateRole(id: User.Id, role: Role)
    fun updateCredentials(id: User.Id, username: Username, email: Email, passwordHash: String)
}
