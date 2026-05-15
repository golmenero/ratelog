package org.raterr.tvshow

import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

class InMemoryTvShowRepository : TvShowRepository {

    private val storage = mutableListOf<TvShow>()
    private val idGenerator = AtomicLong(1)

    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }

    override fun <S : TvShow> save(entity: S): S {
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

    override fun <S : TvShow> saveAll(entities: Iterable<S>): Iterable<S> = entities.map { save(it) }

    override fun findById(id: Long): Optional<TvShow> =
        storage.find { it.id == id }?.let { Optional.of(it) } ?: Optional.empty()

    override fun existsById(id: Long): Boolean = storage.any { it.id == id }

    override fun findAll(): Iterable<TvShow> = storage.toList()

    override fun findAllById(ids: Iterable<Long>): Iterable<TvShow> = storage.filter { it.id in ids }

    override fun count(): Long = storage.size.toLong()

    override fun deleteById(id: Long) {
        storage.removeIf { it.id == id }
    }

    override fun delete(entity: TvShow) {
        storage.removeIf { it.id == entity.id }
    }

    override fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach { deleteById(it) }
    }

    override fun deleteAll(entities: Iterable<TvShow>) {
        entities.forEach { delete(it) }
    }

    override fun deleteAll() {
        storage.clear()
    }

    override fun findByTmdbId(tmdbId: Int): Optional<TvShow> =
        storage.find { it.tmdbId == tmdbId }?.let { Optional.of(it) } ?: Optional.empty()
}
