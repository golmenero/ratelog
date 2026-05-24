package org.raterr.movie

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.Genre
import org.raterr.TmdbId
import org.raterr.movie.get.GetMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbError
import org.raterr.tmdb.TmdbGenre
import org.raterr.tmdb.TmdbMovie

class GetMovieHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val movieRepository = InMemoryMovieRepository()
    private val handler = GetMovieHandler(tmdbClient, movieRepository)

    @BeforeEach
    fun setUp() {
        movieRepository.clear()
    }

    @Test
    fun `creates new movie when not in repo`() {
        whenever(tmdbClient.movieDetails(123)).thenReturn(TmdbMovie(
            id = 123,
            title = "Test Movie",
            originalTitle = "Original",
            overview = "Overview",
            releaseDate = "2024-01-01",
            posterPath = "/poster.jpg",
            voteAverage = 7.5,
            genres = listOf(TmdbGenre(1, "Action"))
        ).right())

        val result = handler.handle(GetMovie(TmdbId(123)))

        assertTrue(result.isRight())
        val saved = movieRepository.findByTmdbId(TmdbId(123))!!
        assertEquals(TmdbId(123), saved.tmdbId)
        assertEquals("Test Movie", saved.title.value)
        assertEquals("Original", saved.originalTitle?.value)
        assertEquals("Overview", saved.overview?.value)
        assertEquals("2024-01-01", saved.releaseDate.toString())
        assertEquals(2024, saved.releaseYear)
        assertEquals("/poster.jpg", saved.posterPath?.value)
        assertEquals(7.5, saved.tmdbVoteAverage)
        assertEquals(listOf(Genre.Action), saved.genres)
    }

    @Test
    fun `updates existing movie when in repo`() {
        movieRepository.save(
            Movie(
                id = Movie.Id(5),
                tmdbId = TmdbId(123),
                title = org.raterr.Title("Old Title"),
                originalTitle = org.raterr.Title("Old Original"),
                overview = org.raterr.Overview("Old Overview"),
                releaseDate = java.time.LocalDate.parse("2020-01-01"),
                releaseYear = 2020,
                posterPath = org.raterr.Url("/old.jpg"),
                tmdbVoteAverage = 5.0,
                genres = listOf(Genre.Drama)
            )
        )
        whenever(tmdbClient.movieDetails(123)).thenReturn(TmdbMovie(
            id = 123,
            title = "New Title",
            originalTitle = "New Original",
            overview = "New Overview",
            releaseDate = "2024-06-01",
            posterPath = "/new.jpg",
            voteAverage = 8.0,
            genres = listOf(TmdbGenre(2, "Comedy"))
        ).right())

        val result = handler.handle(GetMovie(TmdbId(123)))

        assertTrue(result.isRight())
        val saved = movieRepository.findByTmdbId(TmdbId(123))!!
        assertEquals(Movie.Id(5), saved.id)
        assertEquals("New Title", saved.title.value)
        assertEquals(listOf(Genre.Comedy), saved.genres)
    }

    @Test
    fun `returns MovieNotFound when TMDB fails`() {
        whenever(tmdbClient.movieDetails(123)).thenReturn(TmdbError.MovieNotFound.left())

        val result = handler.handle(GetMovie(TmdbId(123)))

        assertTrue(result.isLeft())
    }
}
