package org.raterr.user

import org.raterr.Email
import org.raterr.Username
import java.util.concurrent.atomic.AtomicLong

class InMemoryUserRepository : UserRepository {

    private val storage = mutableListOf<User>()
    private val idGenerator = AtomicLong(1)

    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }

    override fun save(user: User): User {
        return if (user.id == null) {
            val newUser = user.copy(id = User.Id(idGenerator.getAndIncrement()))
            storage.add(newUser)
            newUser
        } else {
            storage.removeIf { it.id == user.id }
            storage.add(user)
            user
        }
    }

    override fun findById(id: User.Id): User? =
        storage.find { it.id == id }

    override fun findByUsername(username: Username): User? =
        storage.find { it.username == username }

    override fun findByEmail(email: Email): User? =
        storage.find { it.email == email }

    override fun existsByUsername(username: Username): Boolean =
        storage.any { it.username == username }

    override fun existsByEmail(email: Email): Boolean =
        storage.any { it.email == email }

    override fun findByUsernameContaining(username: Username): List<User> =
        storage.filter { it.username.value.contains(username.value, ignoreCase = true) }
}
