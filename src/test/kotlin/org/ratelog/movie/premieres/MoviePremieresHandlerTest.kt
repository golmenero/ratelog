package org.ratelog.movie.premieres

import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.TmdbId
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.MovieFactory
import org.ratelog.test.TmdbFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import java.time.LocalDate

class MoviePremieresHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var handler: MoviePremieresHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        handler = MoviePremieresHandler(tmdbClient, movieRepository)
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
        val movie1 = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Released Movie")
        val movie2 = MovieFactory.aMovie(id = 2, tmdbId = 456, title = "Upcoming Movie")
        movieRepository.save(movie1)
        movieRepository.save(movie2)

        val tmdbMovie1 = TmdbFactory.aTmdbMovie(id = 123, title = "Released Movie", releaseDate = "2020-01-01")
        val tmdbMovie2 = TmdbFactory.aTmdbMovie(id = 456, title = "Upcoming Movie", releaseDate = LocalDate.now().plusDays(30).toString())
        whenever(tmdbClient.movieDetails(123)).thenReturn(tmdbMovie1.right())
        whenever(tmdbClient.movieDetails(456)).thenReturn(tmdbMovie2.right())

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
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "No Date Movie")
        movieRepository.save(movie)

        val tmdbMovie = TmdbFactory.aTmdbMovie(id = 123, title = "No Date Movie", releaseDate = null)
        whenever(tmdbClient.movieDetails(123)).thenReturn(tmdbMovie.right())

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
