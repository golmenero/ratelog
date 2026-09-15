package org.ratelog.search.trending.tvshows

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
import org.ratelog.tmdb.TmdbTvShowResponse

class SearchTrendingTvShowsHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val handler = SearchTrendingTvShowsHandler(tmdbClient)

    @Test
    fun `given tmdb returns tv shows when handle then map them to items`() {
        val shows = listOf(
            TmdbTvShowResponse(3, "Show 1", overview = "Overview", firstAirDate = "2023-03-01", posterPath = "/p3.jpg", originalName = "Show 1"),
            TmdbTvShowResponse(4, "Show 2", overview = "Overview", firstAirDate = "2023-04-01", posterPath = "/p4.jpg", originalName = "Show 2"),
        )
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(shows.right())

        val result = handler.handle(SearchTrendingTvShowsQuery(lang = Lang.en))

        assertTrue(result.isRight())
        val trending = result.getOrElse { SearchTrendingTvShows(emptyList()) }
        assertEquals(2, trending.items.size)
        assertEquals("Show 1", trending.items[0].title)
        assertEquals(3, trending.items[0].tmdbId)
        assertEquals("/p3.jpg", trending.items[0].posterPath)
        assertEquals(2023, trending.items[0].year)
        assertEquals("Show 2", trending.items[1].title)
    }

    @Test
    fun `given tmdb returns more than 9 tv shows when handle then limit to 9`() {
        val shows = (1..12).map {
            TmdbTvShowResponse(it, "Show $it", overview = "Overview", firstAirDate = "2023-01-01", posterPath = "/p.jpg", originalName = "Show $it")
        }
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(shows.right())

        val result = handler.handle(SearchTrendingTvShowsQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrendingTvShows(emptyList()) }
        assertEquals(9, trending.items.size)
        assertEquals(9, trending.items.last().tmdbId)
    }

    @Test
    fun `given tmdb returns empty list when handle then return empty items`() {
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(emptyList<TmdbTvShowResponse>().right())

        val result = handler.handle(SearchTrendingTvShowsQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrendingTvShows(emptyList()) }
        assertTrue(trending.items.isEmpty())
    }

    @Test
    fun `given tv show with empty first air date when handle then year is null`() {
        val shows = listOf(TmdbTvShowResponse(2, "Show", firstAirDate = "", posterPath = "/p.jpg", originalName = "Show"))
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(shows.right())

        val result = handler.handle(SearchTrendingTvShowsQuery(lang = Lang.en))

        val trending = result.getOrElse { SearchTrendingTvShows(emptyList()) }
        assertNull(trending.items[0].year)
    }

    @Test
    fun `given tmdb tv shows fail when handle then return left`() {
        whenever(tmdbClient.trendingTvShows(Lang.en)).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(SearchTrendingTvShowsQuery(lang = Lang.en))

        assertTrue(result.isLeft())
    }

    @Test
    fun `given handler when handle then it calls tmdb with provided lang`() {
        whenever(tmdbClient.trendingTvShows(Lang.es)).thenReturn(emptyList<TmdbTvShowResponse>().right())

        handler.handle(SearchTrendingTvShowsQuery(lang = Lang.es))

        verify(tmdbClient).trendingTvShows(Lang.es)
    }
}
