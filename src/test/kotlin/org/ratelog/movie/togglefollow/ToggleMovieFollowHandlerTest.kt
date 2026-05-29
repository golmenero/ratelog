package org.ratelog.movie.togglefollow

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.MovieFactory
import org.ratelog.user.User

class ToggleMovieFollowHandlerTest {

    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var handler: ToggleMovieFollowHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        handler = ToggleMovieFollowHandler(movieRepository)
    }

    @Test
    fun `should follow movie successfully`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Test Movie")
        movieRepository.save(movie)

        val command = ToggleMovieFollow(Movie.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedMovie = movieRepository.findById(Movie.Id(1))
        assertTrue(savedMovie!!.followed)
    }

    @Test
    fun `should return MovieNotFound when movie does not exist`() {
        val command = ToggleMovieFollow(Movie.Id(999), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(ToggleMovieFollowHandlerError.MovieNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should unfollow movie when already followed`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Test Movie", followed = true, followedAtEpochMs = System.currentTimeMillis())
        movieRepository.save(movie)

        val command = ToggleMovieFollow(Movie.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedMovie = movieRepository.findById(Movie.Id(1))
        assertFalse(savedMovie!!.followed)
    }
}
