package org.ratelog.search.trending

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tmdb.TmdbError
import org.ratelog.tmdb.TmdbMovieResponse
import org.ratelog.tmdb.TmdbTvShowResponse

class SearchTrendingHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val handler = SearchTrendingHandler(tmdbClient)

    @Test
    fun `given tmdb returns movies and tv shows when handle then return interleaved items`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie 1", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/p1.jpg", originalTitle = "Movie 1"),
            TmdbMovieResponse(2, "Movie 2", overview = "Overview", releaseDate = "2023-02-01", posterPath = "/p2.jpg", originalTitle = "Movie 2"),
        )
        val shows = listOf(
            TmdbTvShowResponse(3, "Show 1", overview = "Overview", firstAirDate = "2023-03-01", posterPath = "/p3.jpg", originalName = "Show 1"),
            TmdbTvShowResponse(4, "Show 2", overview = "Overview", firstAirDate = "2023-04-01", posterPath = "/p4.jpg", originalName = "Show 2"),
        )
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(shows.right())

        val result = handler.handle(SearchTrendingQuery(lang = Lang.en))

        assertTrue(result.isRight())
        val trending = result.getOrElse { SearchTrending(emptyList()) }
        assertEquals(4, trending.items.size)
        assertEquals("Movie 1", trending.items[0].title)
        assertEquals(MediaType.movie, trending.items[0].type)
        assertEquals("Show 1", trending.items[1].title)
        assertEquals(MediaType.tvshow, trending.items[1].type)
        assertEquals("Movie 2", trending.items[2].title)
        assertEquals("Show 2", trending.items[3].title)
    }

    @Test
    fun `given tmdb returns more than 8 per type when handle then limit each list to 8`() {
        val movies = (1..12).map {
            TmdbMovieResponse(it, "Movie $it", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/p.jpg", originalTitle = "Movie $it")
        }
        val shows = (1..12).map {
            TmdbTvShowResponse(it, "Show $it", overview = "Overview", firstAirDate = "2023-01-01", posterPath = "/p.jpg", originalName = "Show $it")
        }
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(shows.right())

        val result = handler.handle(SearchTrendingQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrending(emptyList()) }
        assertEquals(16, trending.items.size)
        assertEquals(8, trending.items.count { it.type == MediaType.movie })
        assertEquals(8, trending.items.count { it.type == MediaType.tvshow })
    }

    @Test
    fun `given only movies returned when handle then list contains only movies`() {
        val movies = listOf(
            TmdbMovieResponse(1, "Movie", overview = "Overview", releaseDate = "2023-01-01", posterPath = "/p.jpg", originalTitle = "Movie"),
        )
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(emptyList<TmdbTvShowResponse>().right())

        val result = handler.handle(SearchTrendingQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrending(emptyList()) }
        assertEquals(1, trending.items.size)
        assertEquals(MediaType.movie, trending.items[0].type)
    }

    @Test
    fun `given tmdb movies fail when handle then return left`() {
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(TmdbError.MovieNotFound.left())
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(emptyList<TmdbTvShowResponse>().right())

        val result = handler.handle(SearchTrendingQuery(lang = Lang.en))

        assertTrue(result.isLeft())
    }

    @Test
    fun `given tmdb tv shows fail when handle then return left`() {
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(emptyList<TmdbMovieResponse>().right())
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(SearchTrendingQuery(lang = Lang.en))

        assertTrue(result.isLeft())
    }

    @Test
    fun `given items with empty dates when handle then year is null`() {
        val movies = listOf(TmdbMovieResponse(1, "Movie", releaseDate = "", posterPath = "/p.jpg", originalTitle = "Movie"))
        val shows = listOf(TmdbTvShowResponse(2, "Show", firstAirDate = "", posterPath = "/p.jpg", originalName = "Show"))
        whenever(tmdbClient.trendingMovies(Lang.en)).thenReturn(movies.right())
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(shows.right())

        val result = handler.handle(SearchTrendingQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrending(emptyList()) }
        assertNull(trending.items[0].year)
        assertNull(trending.items[1].year)
    }

    @Test
    fun `given handler when handle then it calls tmdb with provided lang`() {
        whenever(tmdbClient.trendingMovies(Lang.es)).thenReturn(emptyList<TmdbMovieResponse>().right())
        whenever(tmdbClient.trendingTvShows(Lang.es)).thenReturn(emptyList<TmdbTvShowResponse>().right())

        handler.handle(SearchTrendingQuery(lang = Lang.es))

        verify(tmdbClient).trendingMovies(Lang.es)
        verify(tmdbClient).trendingTvShows(Lang.es)
    }
}
