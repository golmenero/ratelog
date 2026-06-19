package org.ratelog.search

import arrow.core.getOrElse
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.movie.Movie
import org.ratelog.test.MovieFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.LocalDate

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
            MovieFactory.aMovie(1, 1, "Movie 1", overview = "Overview 1", releaseDate = LocalDate.parse("2023-01-01"), posterPath = "/poster1.jpg", tmdbVoteAverage = 7.5),
            MovieFactory.aMovie(2, 2, "Movie 2", overview = "Overview 2", releaseDate = LocalDate.parse("2023-02-01"), posterPath = "/poster2.jpg", tmdbVoteAverage = 8.0)
        )
        val shows = listOf(
            TvShowFactory.aTvShow(3, 3, "Show 1", overview = "Overview 3", firstAirDate = LocalDate.parse("2023-03-01"), posterPath = "/poster3.jpg", tmdbVoteAverage = 8.5),
            TvShowFactory.aTvShow(4, 4, "Show 2", overview = "Overview 4", firstAirDate = LocalDate.parse("2023-04-01"), posterPath = "/poster4.jpg", tmdbVoteAverage = 9.0)
        )

        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

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
            MovieFactory.aMovie(1, 1, "Movie 1", overview = "Overview 1", releaseDate = LocalDate.parse("2023-01-01"), posterPath = "/poster1.jpg", tmdbVoteAverage = 7.5)
        )

        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TvShow>().right())

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
            TvShowFactory.aTvShow(1, 1, "Show 1", overview = "Overview 1", firstAirDate = LocalDate.parse("2023-01-01"), posterPath = "/poster1.jpg", tmdbVoteAverage = 8.0)
        )

        whenever(tmdbClient.searchMovies("test")).thenReturn(emptyList<Movie>().right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(1, results.size)
        assertEquals("Show 1", results[0].title)
    }

    @Test
    fun `should limit results to 6 per type`() {
        val movies = (1..10).map { MovieFactory.aMovie(it.toLong(), it, "Movie $it", overview = "Overview", releaseDate = LocalDate.parse("2023-01-01"), posterPath = "/poster.jpg", tmdbVoteAverage = 7.5) }
        val shows = (1..10).map { TvShowFactory.aTvShow(it.toLong(), it, "Show $it", overview = "Overview", firstAirDate = LocalDate.parse("2023-01-01"), posterPath = "/poster.jpg", tmdbVoteAverage = 8.0) }

        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

        val query = SearchQuery("test", User.Id(1), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(12, results.size)
    }
}
