package org.ratelog.test

import org.ratelog.Email
import org.ratelog.Username
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryUserRepository : UserRepository {
    private val store = ConcurrentHashMap<User.Id, User>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: User.Id): User? = store[id]

    override fun findByUsername(username: Username): User? =
        store.values.find { it.username == username }

    override fun findByEmail(email: Email): User? =
        store.values.find { it.email == email }

    override fun save(user: User) {
        val userToSave = if (user.id == null) {
            user.copy(id = User.Id(idGenerator.getAndIncrement()))
        } else {
            user
        }
        store[userToSave.id!!] = userToSave
    }

    override fun findByUsernameContaining(username: Username): List<User> =
        store.values.filter { it.username.value.contains(username.value, ignoreCase = true) }

    override fun findByUsernameContaining(username: Username, followerId: User.Id): List<User> {
        val follower = store[followerId] ?: return emptyList()
        return store.values.filter { it.username.value.contains(username.value, ignoreCase = true) }
    }

    override fun findFollowingByUserId(userId: User.Id): List<User> {
        val user = store[userId] ?: return emptyList()
        return store.values.filter { it.followed && it.id != userId }
    }

    override fun findFollowedUserIds(userId: User.Id): List<User.Id> =
        store.values.filter { it.followed && it.id != userId }.mapNotNull { it.id }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
