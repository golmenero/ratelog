package org.ratelog.movie.premieres

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.MovieFactory
import org.ratelog.user.User
import java.time.LocalDate

class MoviePremieresHandlerTest {
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var handler: MoviePremieresHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        handler = MoviePremieresHandler(movieRepository)
    }

    @Test
    fun `should return empty premieres when no followed movies`() {
        val query = MoviePremieresQuery(User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertTrue(premieres.released.isEmpty())
                assertTrue(premieres.upcoming.isEmpty())
                assertTrue(premieres.noDate.isEmpty())
            }
        )
    }

    @Test
    fun `should categorize movies into released and upcoming`() {
        val movie1 = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Released Movie", releaseDate = LocalDate.now().minusDays(1))
        val movie2 = MovieFactory.aMovie(id = 2, tmdbId = 456, title = "Upcoming Movie", releaseDate = LocalDate.now().plusDays(30))
        movieRepository.save(movie1)
        movieRepository.save(movie2)
        movieRepository.toggleFollow(User.Id(1), Movie.Id(1))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(2))

        val query = MoviePremieresQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.released.size)
                assertEquals(1, premieres.upcoming.size)
                assertEquals("Released Movie", premieres.released[0].title)
                assertEquals("Upcoming Movie", premieres.upcoming[0].title)
            }
        )
    }

    @Test
    fun `should categorize movies with no date into noDate list`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "No Date Movie", releaseDate = null)
        movieRepository.save(movie)
        movieRepository.toggleFollow(User.Id(1), Movie.Id(1))

        val query = MoviePremieresQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.noDate.size)
                assertEquals("No Date Movie", premieres.noDate[0].title)
                assertFalse(premieres.noDate[0].hasDate)
            }
        )
    }
}
