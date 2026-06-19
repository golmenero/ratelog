package org.ratelog.tvshow.detail

import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.*
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.TvRatingFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.Instant
import java.time.LocalDate

class DetailTvShowHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: DetailTvShowHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = DetailTvShowHandler(tmdbClient, tvShowRepository, tvRatingRepository)
    }

    @Test
    fun `should return tv show detail when show exists in TMDB`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            name = "Test Show",
            originalName = "Original Name",
            overview = "A great show",
            firstAirDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5,
            genres = listOf(Genre.DRAMA),
            lastSeasonNumber = 2,
        )
        whenever(tmdbClient.tvShowDetails(TmdbId(123), Lang.en)).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals("Test Show", detail.show.name.value)
                assertEquals("Original Name", detail.show.originalName?.value)
                assertEquals("A great show", detail.show.overview?.value)
                assertEquals(2, detail.seasons.size)
            }
        )
    }

    @Test
    fun `should save tv show to repository when fetching details`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            name = "Test Show",
            firstAirDate = LocalDate.parse("2023-01-15"),
        )
        whenever(tmdbClient.tvShowDetails(TmdbId(123), Lang.en)).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.en)
        handler.handle(query)

        val savedShow = tvShowRepository.findByTmdbId(TmdbId(123))
        assertNotNull(savedShow)
        assertEquals("Test Show", savedShow!!.name.value)
    }

    @Test
    fun `should return null overall score when show has no ratings`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            name = "Test Show",
            firstAirDate = LocalDate.parse("2023-01-15"),
        )
        whenever(tmdbClient.tvShowDetails(TmdbId(123), Lang.en)).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertNull(detail.overallScore)
            }
        )
    }

    @Test
    fun `should filter out season 0 from seasons list`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            name = "Test Show",
            firstAirDate = LocalDate.parse("2023-01-15"),
            lastSeasonNumber = 2,
        )
        whenever(tmdbClient.tvShowDetails(TmdbId(123), Lang.en)).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals(2, detail.seasons.size)
                assertEquals(1, detail.seasons[0].seasonNumber)
                assertEquals(2, detail.seasons[1].seasonNumber)
            }
        )
    }
}
