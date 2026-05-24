package org.raterr.movie

import arrow.atomic.value
import org.raterr.TmdbId
import java.util.concurrent.atomic.AtomicLong

class InMemoryMovieRepository : MovieRepository {
    val storage: MutableMap<TmdbId, Movie> = mutableMapOf()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: Movie.Id): Movie? =
        storage.values.firstOrNull { it.id == id }

    override fun findByTmdbId(tmdbId: TmdbId): Movie? =
        storage.values.firstOrNull { it.tmdbId == tmdbId }

    override fun save(movie: Movie): Movie =
        if (movie.id == null) {
            val newEntity = movie.copy(id = idGenerator.getAndIncrement().let(Movie::Id))
            storage[movie.tmdbId] = newEntity

            newEntity
        } else {
            storage.remove(movie.tmdbId)
            storage[movie.tmdbId] = movie

            movie
        }

    fun clear() {
        storage.clear()
    }
}
