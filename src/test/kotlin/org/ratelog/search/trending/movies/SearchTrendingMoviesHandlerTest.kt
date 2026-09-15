package org.ratelog.search.trending.movies

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tmdb.TmdbError
import org.ratelog.tmdb.TmdbMovieResponse

class SearchTrendingMoviesHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val handler = SearchTrendingMoviesHandler(tmdbClient)

    @Test
    fun `given tmdb returns movies when handle then map them to items`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/p1.jpg", originalTitle = "Movie 1"),
            TmdbMovieResponse(2, "Movie 2", overview = "Overview", releaseDate = "2023-02-01", posterPath = "/p2.jpg", originalTitle = "Movie 2"),
        )
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())

        val result = handler.handle(SearchTrendingMoviesQuery(lang = Lang.en))

        assertTrue(result.isRight())
        val trending = result.getOrElse { SearchTrendingMovies(emptyList()) }
        assertEquals(2, trending.items.size)
        assertEquals("Movie 1", trending.items[0].title)
        assertEquals(1, trending.items[0].tmdbId)
        assertEquals("/p1.jpg", trending.items[0].posterPath)
        assertEquals(2023, trending.items[0].year)
        assertEquals("Movie 2", trending.items[1].title)
    }

    @Test
    fun `given tmdb returns more than 9 movies when handle then limit to 9`() {
        val movies = (1..12).map {
            TmdbMovieResponse(it, "Movie $it", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/p.jpg", originalTitle = "Movie $it")
        }
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())

        val result = handler.handle(SearchTrendingMoviesQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrendingMovies(emptyList()) }
        assertEquals(9, trending.items.size)
        assertEquals(9, trending.items.last().tmdbId)
    }

    @Test
    fun `given tmdb returns empty list when handle then return empty items`() {
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(emptyList<TmdbMovieResponse>().right())

        val result = handler.handle(SearchTrendingMoviesQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrendingMovies(emptyList()) }
        assertTrue(trending.items.isEmpty())
    }

    @Test
    fun `given movie with empty release date when handle then year is null`() {
        val movies = listOf(TmdbMovieResponse(1, "Movie", releaseDate = "", posterPath = "/p.jpg", originalTitle = "Movie"))
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())

        val result = handler.handle(SearchTrendingMoviesQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrendingMovies(emptyList()) }
        assertNull(trending.items[0].year)
    }

    @Test
    fun `given tmdb movies fail when handle then return left`() {
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(TmdbError.MovieNotFound.left())

        val result = handler.handle(SearchTrendingMoviesQuery(lang = Lang.en))

        assertTrue(result.isLeft())
    }

    @Test
    fun `given handler when handle then it calls tmdb with provided lang`() {
        whenever(tmdbClient.trendingMovies(Lang.es)).thenReturn(emptyList<TmdbMovieResponse>().right())

        handler.handle(SearchTrendingMoviesQuery(lang = Lang.es))

        verify(tmdbClient).trendingMovies(Lang.es)
    }
}
