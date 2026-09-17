package org.ratelog.test

import org.ratelog.Email
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryUserRepository : UserRepository {
    private val store = ConcurrentHashMap<User.Id, User>()
    private val follows = ConcurrentHashMap.newKeySet<Pair<Long, Long>>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: User.Id): User? = store[id]

    override fun findByUsername(username: Username): User? =
        store.values.find { it.username == username }

    override fun findByEmail(email: Email): User? =
        store.values.find { it.email == email }

    override fun save(user: User): User {
        val userToSave = if (user.id == null) {
            user.copy(id = User.Id(idGenerator.getAndIncrement()))
        } else {
            user
        }
        store[userToSave.id!!] = userToSave
        return userToSave
    }

    override fun findByUsernameContaining(username: Username): List<User> =
        store.values.filter { it.username.value.contains(username.value, ignoreCase = true) }

    override fun findByUsernameContaining(username: Username, followerId: User.Id): List<User> {
        return store.values.filter { it.username.value.contains(username.value, ignoreCase = true) }
    }

    override fun findFollowingByUserId(userId: User.Id): List<User> =
        follows
            .filter { it.first == userId.value }
            .mapNotNull { (_, followedId) -> findById(User.Id(followedId)) }

    override fun isFollowing(followerId: User.Id, followedId: User.Id): Boolean =
        (followerId.value to followedId.value) in follows

    override fun toggleFollow(followerId: User.Id, followedId: User.Id) {
        val pair = followerId.value to followedId.value
        if (pair in follows) follows.remove(pair) else follows.add(pair)
    }

    override fun findAll(): List<User> =
        store.values.sortedByDescending { it.createdAtEpochMs }

    override fun deleteById(id: User.Id) {
        store.remove(id)
    }

    override fun updateRole(id: User.Id, role: Role) {
        val existing = store[id] ?: return
        store[id] = existing.copy(role = role)
    }

    override fun updateCredentials(id: User.Id, username: Username, email: Email, passwordHash: String) {
        val existing = store[id] ?: return
        store[id] = existing.copy(username = username, email = email, passwordHash = passwordHash)
    }
}
