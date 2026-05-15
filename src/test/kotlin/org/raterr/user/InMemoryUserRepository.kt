package org.raterr.user

import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryUserRepository : UserRepository {

    private val storage = mutableListOf<User>()
    private val idGenerator = AtomicLong(1)

    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }

    override fun <S : User> save(entity: S): S {
        @Suppress("UNCHECKED_CAST")
        return if (entity.id == null) {
            val newUser = entity.copy(id = idGenerator.getAndIncrement()) as S
            storage.add(newUser)
            newUser
        } else {
            storage.removeIf { it.id == entity.id }
            storage.add(entity)
            entity
        }
    }

    override fun <S : User> saveAll(entities: Iterable<S>): Iterable<S> = entities.map { save(it) }

    override fun findById(id: Long): Optional<User> =
        storage.find { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsById(id: Long): Boolean = storage.any { it.id == id }

    override fun findAll(): Iterable<User> = storage.toList()

    override fun findAllById(ids: Iterable<Long>): Iterable<User> = storage.filter { it.id in ids }

    override fun count(): Long = storage.size.toLong()

    override fun deleteById(id: Long) {
        storage.removeIf { it.id == id }
    }

    override fun delete(entity: User) {
        storage.removeIf { it.id == entity.id }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { deleteById(it) }
    }

    override fun deleteAll(entities: Iterable<User>) {
        entities.forEach { delete(it) }
    }

    override fun deleteAll() {
        storage.clear()
    }

    override fun findByUsername(username: String): Optional<User> =
        storage.find { it.username == username }?.let { Optional.of(it) } ?: Optional.empty()

    override fun findByEmail(email: String): Optional<User> =
        storage.find { it.email == email }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsByUsername(username: String): Boolean = storage.any { it.username == username }

    override fun existsByEmail(email: String): Boolean = storage.any { it.email == email }

    override fun findByUsernameContaining(username: String): List<User> =
        storage.filter { it.username.contains(username, ignoreCase = true) }
}
