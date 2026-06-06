package org.ratelog.movie.detail

import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.*
import org.ratelog.movie.Movie
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.InMemoryRatingRepository
import org.ratelog.test.MovieFactory
import org.ratelog.test.RatingFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import java.time.Instant
import java.time.LocalDate

class DetailMovieHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var handler: DetailMovieHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        ratingRepository = InMemoryRatingRepository()
        handler = DetailMovieHandler(tmdbClient, movieRepository, ratingRepository)
    }

    @Test
    fun `should return movie detail when movie exists in TMDB`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            title = "Test Movie",
            originalTitle = "Original Title",
            overview = "A great movie",
            releaseDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5,
            genres = listOf(Genre.ACTION)
        )
        whenever(tmdbClient.movieDetails(123)).thenReturn(tmdbMovie.right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals("Test Movie", detail.movie.title.value)
                assertEquals("Original Title", detail.movie.originalTitle?.value)
                assertEquals("A great movie", detail.movie.overview?.value)
            }
        )
    }

    @Test
    fun `should save movie to repository when fetching details`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            title = "Test Movie",
            releaseDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5
        )
        whenever(tmdbClient.movieDetails(123)).thenReturn(tmdbMovie.right())

        val query = GetMovieDetail(User.Id(1),TmdbId(123))
        handler.handle(query)

        val savedMovie = movieRepository.findByTmdbId(TmdbId(123))
        assertNotNull(savedMovie)
        assertEquals("Test Movie", savedMovie!!.title.value)
    }

    @Test
    fun `should return rating info when movie has ratings`() {
        val tmdbMovie = MovieFactory.aMovie(id = 123, title = "Test Movie", releaseDate = LocalDate.parse("2023-01-15"))
        whenever(tmdbClient.movieDetails(123)).thenReturn(tmdbMovie.right())

        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Test Movie")
        movieRepository.save(movie)

        val rating = RatingFactory.aRating(
            id = 1,
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 6.0,
            acting = 7.0,
            soundtrack = 8.0,
            screenplay = 9.0,
            createdAt = Instant.now()
        )
        ratingRepository.save(rating)

        val query = GetMovieDetail(User.Id(1), TmdbId(123))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals(5.0, detail.directing)
                assertEquals(6.0, detail.cinematography)
                assertEquals(7.0, detail.acting)
                assertEquals(8.0, detail.soundtrack)
                assertEquals(9.0, detail.screenplay)
                assertEquals(7.0, detail.score)
            }
        )
    }

    @Test
    fun `should return null rating fields when movie has no ratings`() {
        val tmdbMovie = MovieFactory.aMovie(id = 123, title = "Test Movie", releaseDate = LocalDate.parse("2023-01-15"))
        whenever(tmdbClient.movieDetails(123)).thenReturn(tmdbMovie.right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertNull(detail.directing)
                assertNull(detail.cinematography)
                assertNull(detail.acting)
                assertNull(detail.soundtrack)
                assertNull(detail.screenplay)
                assertNull(detail.score)
            }
        )
    }
}
