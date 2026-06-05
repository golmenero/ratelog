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
    private val follows = ConcurrentHashMap<Pair<Long, Long>, Boolean>()
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
        store.values.filter { follows[Pair(userId.value, it.id!!.value)] == true }

    override fun findAll(): List<Movie> =
        store.values.toList()

    override fun isFollowed(userId: User.Id, movieId: Movie.Id): Boolean =
        follows[Pair(userId.value, movieId.value)] == true

    override fun toggleFollow(movieId: Movie.Id) {
        val key = Pair(1L, movieId.value)
        follows[key] = follows[key] != true
    }

    fun clear() {
        store.clear()
        follows.clear()
        idGenerator.set(1)
    }
}
