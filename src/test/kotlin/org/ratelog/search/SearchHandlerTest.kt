package org.ratelog.search

import arrow.core.getOrElse
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.MediaType
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
        val query = SearchQuery("", User.Id(1), Lang.en, null)

        val result = handler.handle(query)

        assertTrue(result.isRight())
        assertEquals(0, result.getOrElse { SearchResult(emptyList(), false) }.items.size)
        assertFalse(result.getOrElse { SearchResult(emptyList(), false) }.hasMore)
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

        whenever(tmdbClient.searchMovies("test", Lang.en, 1)).thenReturn((movies to 1).right())
        whenever(tmdbClient.searchTvShows("test", Lang.en, 1)).thenReturn((shows to 1).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, null)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(4, results.items.size)
        assertEquals("Movie 1", results.items[0].title)
        assertEquals("Show 1", results.items[1].title)
        assertEquals("Movie 2", results.items[2].title)
        assertEquals("Show 2", results.items[3].title)
        assertFalse(results.hasMore)
    }

    @Test
    fun `should return only movies when no tv shows found`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5, originalTitle = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en, 1)).thenReturn((movies to 1).right())
        whenever(tmdbClient.searchTvShows("test", Lang.en, 1)).thenReturn((emptyList<TmdbTvShowResponse>() to 1).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, null)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(1, results.items.size)
        assertEquals("Movie 1", results.items[0].title)
    }

    @Test
    fun `should return only tv shows when no movies found`() {
        val shows = listOf(
            TmdbTvShowResponse(1, "Show 1", overview = "Overview 1", firstAirDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 8.0, originalName = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en, 1)).thenReturn((emptyList<TmdbMovieResponse>() to 1).right())
        whenever(tmdbClient.searchTvShows("test", Lang.en, 1)).thenReturn((shows to 1).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, null)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(1, results.items.size)
        assertEquals("Show 1", results.items[0].title)
    }

    @Test
    fun `should indicate hasMore when there are additional pages`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5, originalTitle = "a-title")
        )
        val shows = listOf(
            TmdbTvShowResponse(2, "Show 1", overview = "Overview 2", firstAirDate = "2023-02-01", posterPath = "/poster2.jpg", voteAverage = 8.0, originalName = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en, 1)).thenReturn((movies to 3).right())
        whenever(tmdbClient.searchTvShows("test", Lang.en, 1)).thenReturn((shows to 2).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, null)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(2, results.items.size)
        assertTrue(results.hasMore)
    }

    @Test
    fun `should filter by movie type only`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5, originalTitle = "a-title"),
            TmdbMovieResponse(2, "Movie 2", overview = "Overview 2", releaseDate = "2023-02-01", posterPath = "/poster2.jpg", voteAverage = 8.0, originalTitle = "a-title")
        )

        whenever(tmdbClient.searchMovies("test", Lang.en, 1)).thenReturn((movies to 1).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, MediaType.movie)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(2, results.items.size)
        assertEquals("Movie 1", results.items[0].title)
        assertEquals("Movie 2", results.items[1].title)
        assertFalse(results.hasMore)
    }

    @Test
    fun `should filter by tv show type only`() {
        val shows = listOf(
            TmdbTvShowResponse(1, "Show 1", overview = "Overview 1", firstAirDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 8.0, originalName = "a-title"),
            TmdbTvShowResponse(2, "Show 2", overview = "Overview 2", firstAirDate = "2023-02-01", posterPath = "/poster2.jpg", voteAverage = 9.0, originalName = "a-title")
        )

        whenever(tmdbClient.searchTvShows("test", Lang.en, 1)).thenReturn((shows to 2).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, MediaType.tvshow)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(2, results.items.size)
        assertEquals("Show 1", results.items[0].title)
        assertEquals("Show 2", results.items[1].title)
        assertTrue(results.hasMore)
    }

    @Test
    fun `should limit results to 9 items per page`() {
        val movies = (1..10).map { TmdbMovieResponse(it, "Movie $it", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/poster.jpg", voteAverage = 7.5, originalTitle = "a-title") }
        val shows = (1..10).map { TmdbTvShowResponse(it, "Show $it", overview = "Overview", firstAirDate = "2023-01-01", posterPath = "/poster.jpg", voteAverage = 8.0, originalName = "a-title") }

        whenever(tmdbClient.searchMovies("test", Lang.en, 1)).thenReturn((movies to 1).right())
        whenever(tmdbClient.searchTvShows("test", Lang.en, 1)).thenReturn((shows to 1).right())

        val query = SearchQuery("test", User.Id(1), Lang.en, null)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { SearchResult(emptyList(), false) }
        assertEquals(9, results.items.size)
        assertTrue(results.hasMore)
    }
}
