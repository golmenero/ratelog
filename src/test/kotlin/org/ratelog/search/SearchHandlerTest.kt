package org.ratelog.search

import arrow.core.getOrElse
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.Title
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tmdb.TmdbMovieResponse
import org.ratelog.tmdb.TmdbTvShowResponse
import org.ratelog.user.User

class SearchHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val handler = SearchHandler(tmdbClient)

    @Test
    fun `should return empty list when query is blank`() {
        val query = SearchQuery("", User.Id(1), Lang.en)

        val result = handler.handle(query)

        assertTrue(result.isRight())
        assertEquals(0, result.getOrElse { emptyList() }.size)
    }

    @Test
    fun `should return interleaved movie and tv show results`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5, originalTitle = "a-title"),
            TmdbMovieResponse(2, "Movie 2", overview = "Overview 2", releaseDate = "2023-02-01", posterPath = "/poster2.jpg", voteAverage = 8.0, originalTitle = "a-title")
        )
        val shows = listOf(
            TmdbTvShowResponse(3, "Show 1", overview = "Overview 3", firstAirDate = "2023-03-01", posterPath = "/poster3.jpg", voteAverage = 8.5, originalName = "a-title"),
            TmdbTvShowResponse(4, "Show 2", overview = "Overview 4", firstAirDate = "2023-04-01", posterPath = "/poster4.jpg", voteAverage = 9.0, originalName = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test", Lang.en)).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(4, results.size)
        assertEquals("Movie 1", results[0].title)
        assertEquals("Show 1", results[1].title)
        assertEquals("Movie 2", results[2].title)
        assertEquals("Show 2", results[3].title)
    }

    @Test
    fun `should return only movies when no tv shows found`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5, originalTitle = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test", Lang.en)).thenReturn(emptyList<TmdbTvShowResponse>().right())

        val query = SearchQuery("test", User.Id(1), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(1, results.size)
        assertEquals("Movie 1", results[0].title)
    }

    @Test
    fun `should return only tv shows when no movies found`() {
        val shows = listOf(
            TmdbTvShowResponse(1, "Show 1", overview = "Overview 1", firstAirDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 8.0, originalName = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en)).thenReturn(emptyList<TmdbMovieResponse>().right())
        whenever(tmdbClient.searchTvShows("test", Lang.en)).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(1, results.size)
        assertEquals("Show 1", results[0].title)
    }

    @Test
    fun `should limit results to 6 per type`() {
        val movies = (1..10).map { TmdbMovieResponse(it, "Movie $it", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/poster.jpg", voteAverage = 7.5, originalTitle = "a-title") }
        val shows = (1..10).map { TmdbTvShowResponse(it, "Show $it", overview = "Overview", firstAirDate = "2023-01-01", posterPath = "/poster.jpg", voteAverage = 8.0, originalName = "a-title") }

        whenever(tmdbClient.searchMovies("test", Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test", Lang.en)).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(12, results.size)
    }
}
