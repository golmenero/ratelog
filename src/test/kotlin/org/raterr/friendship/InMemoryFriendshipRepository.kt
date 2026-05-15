package org.raterr.friendship

import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryFriendshipRepository : FriendshipRepository {

    private val storage = mutableListOf<Friendship>()
    private val idGenerator = AtomicLong(1)

    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }

    override fun existsByUserIdAndFriendId(userId: Long, friendId: Long): Boolean =
        storage.any { it.userId == userId && it.friendId == friendId }

    override fun findPendingRequest(
        requesterId: Long,
        receiverId: Long
    ): Optional<Friendship> =
        storage
            .firstOrNull { it.userId == requesterId && it.friendId == receiverId && it.status == "pending" }
            .let { Optional.ofNullable(it) }

    override fun findAllFriendsByUserId(userId: Long): List<Friendship> =
        storage.filter {
            (it.userId == userId || it.friendId == userId) && it.status == "accepted"
        }

    override fun <S : Friendship> save(entity: S): S {
        @Suppress("UNCHECKED_CAST")
        return if (entity.id == null) {
            val newEntity = entity.copy(id = idGenerator.getAndIncrement()) as S
            storage.add(newEntity)
            newEntity
        } else {
            storage.removeIf { it.id == entity.id }
            storage.add(entity)
            entity
        }
    }

    override fun <S : Friendship> saveAll(entities: Iterable<S>): Iterable<S> = entities.map { save(it) }

    override fun findById(id: Long): Optional<Friendship> =
        storage.find { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsById(id: Long): Boolean = storage.any { it.id == id }

    override fun findAll(): Iterable<Friendship> = storage.toList()

    override fun findAllById(ids: Iterable<Long>): Iterable<Friendship> = storage.filter { it.id in ids }

    override fun count(): Long = storage.size.toLong()

    override fun deleteById(id: Long) {
        storage.removeIf { it.id == id }
    }

    override fun delete(entity: Friendship) {
        storage.removeIf { it.id == entity.id }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { deleteById(it) }
    }

    override fun deleteAll(entities: Iterable<Friendship>) {
        entities.forEach { delete(it) }
    }

    override fun deleteAll() {
        storage.clear()
    }
}
