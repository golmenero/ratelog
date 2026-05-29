package org.ratelog.user

import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Username

data class User(
    val id: Id?,
    val username: Username,
    val email: Email,
    val passwordHash: String,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val followed: Boolean,
    val followedAtEpochMs: Long?,
    val lang: Lang,
) {
    data class Id(val value: Long)

    fun toggleFollow(now: Long) =
        if (followed) copy(followed = false, followedAtEpochMs = null)
        else copy(followed = true, followedAtEpochMs = now)
}

interface UserRepository {
    fun findById(id: User.Id): User?
    fun findByUsername(username: Username): User?
    fun findByEmail(email: Email): User?
    fun save(user: User)
    fun findByUsernameContaining(username: Username): List<User>
    fun findByUsernameContaining(username: Username, followerId: User.Id): List<User>

    fun findFollowingByUserId(userId: User.Id): List<User>
    fun findFollowedUserIds(userId: User.Id): List<User.Id>
}
