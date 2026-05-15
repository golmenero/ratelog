package org.raterr.follow

import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryFollowRepository : FollowRepository {

    private val storage = mutableListOf<Follow>()
    private val idGenerator = AtomicLong(1)

    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }

    override fun findByUserIdAndContentTypeAndContentTmdbId(
        userId: Long,
        contentType: String,
        contentTmdbId: Int
    ): Optional<Follow> =
        storage
            .firstOrNull { it.userId == userId && it.contentType == contentType && it.contentTmdbId == contentTmdbId }
            .let { Optional.ofNullable(it) }

    override fun existsByUserIdAndContentTypeAndContentTmdbId(
        userId: Long,
        contentType: String,
        contentTmdbId: Int
    ): Boolean =
        storage.any { it.userId == userId && it.contentType == contentType && it.contentTmdbId == contentTmdbId }

    override fun findByUserId(userId: Long): List<Follow> =
        storage.filter { it.userId == userId }

    override fun <S : Follow> save(entity: S): S {
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

    override fun <S : Follow> saveAll(entities: Iterable<S>): Iterable<S> = entities.map { save(it) }

    override fun findById(id: Long): Optional<Follow> =
        storage.find { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsById(id: Long): Boolean = storage.any { it.id == id }

    override fun findAll(): Iterable<Follow> = storage.toList()

    override fun findAllById(ids: Iterable<Long>): Iterable<Follow> = storage.filter { it.id in ids }

    override fun count(): Long = storage.size.toLong()

    override fun deleteById(id: Long) {
        storage.removeIf { it.id == id }
    }

    override fun delete(entity: Follow) {
        storage.removeIf { it.id == entity.id }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { deleteById(it) }
    }

    override fun deleteAll(entities: Iterable<Follow>) {
        entities.forEach { delete(it) }
    }

    override fun deleteAll() {
        storage.clear()
    }
}
