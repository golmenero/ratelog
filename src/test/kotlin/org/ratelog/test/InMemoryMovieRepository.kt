package org.ratelog.test

import org.ratelog.*
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieRepository
import org.ratelog.user.User
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryMovieRepository : MovieRepository {
    private val store = ConcurrentHashMap<Movie.Id, Movie>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: Movie.Id): Movie? = store[id]

    override fun findByTmdbId(tmdbId: TmdbId): Movie? =
        store.values.find { it.tmdbId == tmdbId }

    override fun save(movie: Movie) {
        val movieToSave = if (movie.id == null) {
            movie.copy(id = Movie.Id(idGenerator.getAndIncrement()))
        } else {
            movie
        }
        store[movieToSave.id!!] = movieToSave
    }

    override fun findFollowedMovies(userId: User.Id): List<Movie> =
        store.values.filter { it.followed }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
