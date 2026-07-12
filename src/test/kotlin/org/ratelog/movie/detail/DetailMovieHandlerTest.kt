package org.ratelog.movie.detail

import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.ratelog.*
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieDescription
import org.ratelog.test.InMemoryMovieDescriptionRepository
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.InMemoryRatingRepository
import org.ratelog.test.MovieFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import java.time.LocalDate

class DetailMovieHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var movieDescriptionRepository: InMemoryMovieDescriptionRepository
    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var handler: DetailMovieHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        movieDescriptionRepository = InMemoryMovieDescriptionRepository()
        ratingRepository = InMemoryRatingRepository()
        handler = DetailMovieHandler(tmdbClient, movieRepository, movieDescriptionRepository, ratingRepository)
    }

    @Test
    fun `should return movie detail with translated title when description exists for user lang`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            tmdbId = 123,
            originalTitle = "Original Title",
            releaseDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5,
            genres = listOf(Genre.ACTION)
        )
        movieRepository.save(tmdbMovie)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null,TmdbId(123), Lang.en, Title("Translated Title"), Overview("Translated overview"))
        ))

        whenever(tmdbClient.movieDetails(TmdbId(123))).thenReturn(tmdbMovie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(123), Title("a-title"))).thenReturn(emptyList<MovieDescription>().right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals("Translated Title", detail.title)
                assertEquals("Translated overview", detail.overview)
                assertEquals("Original Title", detail.originalTitle)
            }
        )
    }

    @Test
    fun `should fallback to original title when no description exists for user lang`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            tmdbId = 123,
            originalTitle = "Original Title",
            releaseDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5,
        )
        movieRepository.save(tmdbMovie)

        val translations = listOf(
            MovieDescription(null,TmdbId(123), Lang.en, Title("EN Title"), Overview("EN Overview"))
        )
        whenever(tmdbClient.movieDetails(TmdbId(123))).thenReturn(tmdbMovie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(123), Title("Original Title"))).thenReturn(translations.right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123), Lang.es)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals("Original Title", detail.title)
                assertNull(detail.overview)
            }
        )
    }

    @Test
    fun `should save movie to repository when fetching details`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            tmdbId = 123,
            originalTitle = "Test Movie",
            releaseDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5
        )
        whenever(tmdbClient.movieDetails(TmdbId(123))).thenReturn(tmdbMovie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(123), Title("Test Movie"))).thenReturn(emptyList<MovieDescription>().right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123), Lang.en)
        handler.handle(query)

        val savedMovie = movieRepository.findByTmdbId(TmdbId(123))
        assertNotNull(savedMovie)
        assertEquals("Test Movie", savedMovie!!.originalTitle.value)
    }

    @Test
    fun `should return null rating fields when movie has no ratings`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            tmdbId = 123,
            originalTitle = "Test Movie",
            releaseDate = LocalDate.parse("2023-01-15")
        )
        movieRepository.save(tmdbMovie)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null,TmdbId(123), Lang.en, Title("Test Movie"), null)
        ))

        whenever(tmdbClient.movieDetails(TmdbId(123))).thenReturn(tmdbMovie.right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123), Lang.en)
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

    @Test
    fun `should persist translations when no descriptions exist`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            tmdbId = 123,
            originalTitle = "Test Movie",
            releaseDate = LocalDate.parse("2023-01-15"),
        )
        val translations = listOf(
            MovieDescription(null,TmdbId(123), Lang.en, Title("EN Title"), Overview("EN Overview")),
            MovieDescription(null,TmdbId(123), Lang.es, Title("ES Title"), Overview("ES Overview")),
        )
        whenever(tmdbClient.movieDetails(TmdbId(123))).thenReturn(tmdbMovie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(123), Title("Test Movie"))).thenReturn(translations.right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123), Lang.en)
        handler.handle(query)

        val saved = movieDescriptionRepository.findAllByTmdbId(TmdbId(123))
        assertEquals(2, saved.size)
    }

    @Test
    fun `should not fetch translations when descriptions already exist`() {
        val tmdbMovie = MovieFactory.aMovie(
            id = 123,
            tmdbId = 123,
            originalTitle = "Test Movie",
            releaseDate = LocalDate.parse("2023-01-15"),
        )
        movieRepository.save(tmdbMovie)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null,TmdbId(123), Lang.en, Title("EN Title"), Overview("EN Overview"))
        ))

        whenever(tmdbClient.movieDetails(TmdbId(123))).thenReturn(tmdbMovie.right())

        val query = GetMovieDetail(User.Id(1), TmdbId(123), Lang.en)
        handler.handle(query)

        verify(tmdbClient, org.mockito.kotlin.never()).movieTranslations(TmdbId(123), Title("a-title"))
    }
}
