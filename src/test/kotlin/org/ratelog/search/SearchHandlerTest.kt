package org.ratelog.search

import arrow.core.getOrElse
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.test.TmdbFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tmdb.TmdbMovie
import org.ratelog.tmdb.TmdbTvShow
import org.ratelog.user.User

class SearchHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val handler = SearchHandler(tmdbClient)

    @Test
    fun `should return empty list when query is blank`() {
        val query = SearchQuery("", User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isRight())
        assertEquals(0, result.getOrElse { emptyList() }.size)
    }

    @Test
    fun `should return interleaved movie and tv show results`() {
        val movies = listOf(
            TmdbFactory.aTmdbMovie(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5),
            TmdbFactory.aTmdbMovie(2, "Movie 2", overview = "Overview 2", releaseDate = "2023-02-01", posterPath = "/poster2.jpg", voteAverage = 8.0)
        )
        val shows = listOf(
            TmdbFactory.aTmdbTvShow(3, "Show 1", overview = "Overview 3", firstAirDate = "2023-03-01", posterPath = "/poster3.jpg", voteAverage = 8.5),
            TmdbFactory.aTmdbTvShow(4, "Show 2", overview = "Overview 4", firstAirDate = "2023-04-01", posterPath = "/poster4.jpg", voteAverage = 9.0)
        )

        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1))
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
            TmdbFactory.aTmdbMovie(1, "Movie 1", overview = "Overview 1", releaseDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 7.5)
        )

        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val query = SearchQuery("test", User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(1, results.size)
        assertEquals("Movie 1", results[0].title)
    }

    @Test
    fun `should return only tv shows when no movies found`() {
        val shows = listOf(
            TmdbFactory.aTmdbTvShow(1, "Show 1", overview = "Overview 1", firstAirDate = "2023-01-01", posterPath = "/poster1.jpg", voteAverage = 8.0)
        )

        whenever(tmdbClient.searchMovies("test")).thenReturn(emptyList<TmdbMovie>().right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(1, results.size)
        assertEquals("Show 1", results[0].title)
    }

    @Test
    fun `should limit results to 6 per type`() {
        val movies = (1..10).map { TmdbFactory.aTmdbMovie(it, "Movie $it", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/poster.jpg", voteAverage = 7.5) }
        val shows = (1..10).map { TmdbFactory.aTmdbTvShow(it, "Show $it", overview = "Overview", firstAirDate = "2023-01-01", posterPath = "/poster.jpg", voteAverage = 8.0) }

        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(12, results.size)
    }
}
