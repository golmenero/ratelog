package org.raterr.follow

import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryUserFollowRepository : UserFollowRepository {

    private val storage = mutableListOf<UserFollow>()
    private val users = mutableListOf<UserFollowUser>()
    private val idGenerator = AtomicLong(1)

    data class UserFollowUser(
        val id: Long,
        val username: String
    )

    fun addUser(id: Long, username: String) {
        users.add(UserFollowUser(id, username))
    }

    fun clear() {
        storage.clear()
        users.clear()
        idGenerator.set(1)
    }

    override fun existsByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Boolean =
        storage.any { it.followerId == followerId && it.followedId == followedId }

    override fun findByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Optional<UserFollow> =
        storage
            .firstOrNull { it.followerId == followerId && it.followedId == followedId }
            .let { Optional.ofNullable(it) }

    override fun findFollowingByUserId(userId: Long): List<UserFollowWithUsername> =
        storage
            .filter { it.followerId == userId }
            .mapNotNull { follow ->
                users.find { it.id == follow.followedId }?.let { user ->
                    UserFollowWithUsername(
                        id = follow.id,
                        followerId = follow.followerId,
                        followedId = follow.followedId,
                        createdAtEpochMs = follow.createdAtEpochMs,
                        followedUsername = user.username
                    )
                }
            }

    override fun findFollowersByUserId(userId: Long): List<UserFollow> =
        storage.filter { it.followedId == userId }

    override fun <S : UserFollow> save(entity: S): S {
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

    override fun <S : UserFollow> saveAll(entities: Iterable<S>): Iterable<S> = entities.map { save(it) }

    override fun findById(id: Long): Optional<UserFollow> =
        storage.find { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsById(id: Long): Boolean = storage.any { it.id == id }

    override fun findAll(): Iterable<UserFollow> = storage.toList()

    override fun findAllById(ids: Iterable<Long>): Iterable<UserFollow> = storage.filter { it.id in ids }

    override fun count(): Long = storage.size.toLong()

    override fun deleteById(id: Long) {
        storage.removeIf { it.id == id }
    }

    override fun delete(entity: UserFollow) {
        storage.removeIf { it.id == entity.id }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { deleteById(it) }
    }

    override fun deleteAll(entities: Iterable<UserFollow>) {
        entities.forEach { delete(it) }
    }

    override fun deleteAll() {
        storage.clear()
    }
}
