package org.raterr.movie

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.TmdbId
import org.raterr.movie.get.GetMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.tmdb.FakeTmdbClient
import org.raterr.tmdb.TmdbGenre
import org.raterr.tmdb.TmdbMovie

class GetMovieHandlerTest {

    private val movieRepository = InMemoryMovieRepository()
    private lateinit var tmdbClient: FakeTmdbClient
    private lateinit var handler: GetMovieHandler

    @BeforeEach
    fun setUp() {
        movieRepository.clear()
    }

    @Test
    fun `creates new movie when not in repo`() {
        tmdbClient = FakeTmdbClient(movies = mapOf(
            123 to TmdbMovie(
                id = 123,
                title = "Test Movie",
                originalTitle = "Original",
                overview = "Overview",
                releaseDate = "2024-01-01",
                posterPath = "/poster.jpg",
                voteAverage = 7.5,
                genres = listOf(TmdbGenre(1, "Action"))
            )
        ))
        handler = GetMovieHandler(tmdbClient, movieRepository)

        val result = handler.handle(GetMovie(TmdbId(123)))

        assertTrue(result.isRight())
        val saved = movieRepository.findByTmdbId(123).get()
        assertEquals(123, saved.tmdbId)
        assertEquals("Test Movie", saved.title)
        assertEquals("Original", saved.originalTitle)
        assertEquals("Overview", saved.overview)
        assertEquals("2024-01-01", saved.releaseDate)
        assertEquals(2024, saved.releaseYear)
        assertEquals("/poster.jpg", saved.posterPath)
        assertEquals(7.5, saved.tmdbVoteAverage)
        assertEquals("Action", saved.genres)
    }

    @Test
    fun `updates existing movie when in repo`() {
        movieRepository.save(
            Movie(
                id = 5,
                tmdbId = 123,
                title = "Old Title",
                originalTitle = "Old Original",
                overview = "Old Overview",
                releaseDate = "2020-01-01",
                releaseYear = 2020,
                posterPath = "/old.jpg",
                tmdbVoteAverage = 5.0,
                genres = "Drama"
            )
        )
        tmdbClient = FakeTmdbClient(movies = mapOf(
            123 to TmdbMovie(
                id = 123,
                title = "New Title",
                originalTitle = "New Original",
                overview = "New Overview",
                releaseDate = "2024-06-01",
                posterPath = "/new.jpg",
                voteAverage = 8.0,
                genres = listOf(TmdbGenre(2, "Comedy"))
            )
        ))
        handler = GetMovieHandler(tmdbClient, movieRepository)

        val result = handler.handle(GetMovie(TmdbId(123)))

        assertTrue(result.isRight())
        val saved = movieRepository.findByTmdbId(123).get()
        assertEquals(5, saved.id)
        assertEquals("New Title", saved.title)
        assertEquals("Comedy", saved.genres)
    }

    @Test
    fun `returns MovieNotFound when TMDB fails`() {
        tmdbClient = FakeTmdbClient()
        handler = GetMovieHandler(tmdbClient, movieRepository)

        val result = handler.handle(GetMovie(TmdbId(999)))

        assertTrue(result.isLeft())
    }
}
