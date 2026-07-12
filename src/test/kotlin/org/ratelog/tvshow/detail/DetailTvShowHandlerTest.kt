package org.ratelog.tvshow.detail

import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.*
import org.ratelog.test.InMemoryTvDescriptionRepository
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.TvRatingFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.TvDescription
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.Instant
import java.time.LocalDate

class DetailTvShowHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var tvDescriptionRepository: InMemoryTvDescriptionRepository
    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: DetailTvShowHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        tvDescriptionRepository = InMemoryTvDescriptionRepository()
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = DetailTvShowHandler(tmdbClient, tvShowRepository, tvDescriptionRepository, tvRatingRepository)
    }

    @Test
    fun `should return tv show detail with translated name when description exists for user lang`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            originalName = "Original Name",
            firstAirDate = LocalDate.parse("2023-01-15"),
            posterPath = "/poster.jpg",
            tmdbVoteAverage = 7.5,
            genres = listOf(Genre.DRAMA),
            lastSeasonNumber = 2,
        )
        tvShowRepository.save(tmdbShow)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(TmdbId(123), Lang.en, Title("Translated Name"), Overview("Translated overview"))
        ))

        whenever(tmdbClient.tvShowDetails(TmdbId(123))).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.en)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals("Translated Name", detail.title)
                assertEquals("Translated overview", detail.overview)
                assertEquals("Original Name", detail.originalTitle)
                assertEquals(2, detail.seasons.size)
            }
        )
    }

    @Test
    fun `should fallback to original name when no description exists for user lang`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            originalName = "Original Name",
            firstAirDate = LocalDate.parse("2023-01-15"),
            lastSeasonNumber = 2,
        )
        tvShowRepository.save(tmdbShow)

        whenever(tmdbClient.tvShowDetails(TmdbId(123))).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.es)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { detail ->
                assertEquals("Original Name", detail.title)
                assertNull(detail.overview)
            }
        )
    }

    @Test
    fun `should save tv show to repository when fetching details`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            originalName = "Test Show",
            firstAirDate = LocalDate.parse("2023-01-15"),
        )
        whenever(tmdbClient.tvShowDetails(TmdbId(123))).thenReturn(tmdbShow.right())

        val query = GetTvShowDetail(User.Id(1), TmdbId(123), Lang.en)
        handler.handle(query)

        val savedShow = tvShowRepository.findByTmdbId(TmdbId(123))
        assertNotNull(savedShow)
        assertEquals("Test Show", savedShow!!.originalName?.value)
    }

    @Test
    fun `should return null overall score when show has no ratings`() {
        val tmdbShow = TvShowFactory.aTvShow(
            tmdbId = 123,
            id = 123,
            originalName = "Test Show",
            firstAirDate = LocalDate.parse("2023-01-15"),
        )
        tvShowRepository.save(tmdbShow)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(TmdbId(123), Lang.en, Title("Test Show"), null)
        ))

        whenever(tmdbClient.tvShowDetails(TmdbId(123))).thenReturn(tmdbShow.right())

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
            originalName = "Test Show",
            firstAirDate = LocalDate.parse("2023-01-15"),
            lastSeasonNumber = 2,
        )
        tvShowRepository.save(tmdbShow)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(TmdbId(123), Lang.en, Title("Test Show"), null)
        ))

        whenever(tmdbClient.tvShowDetails(TmdbId(123))).thenReturn(tmdbShow.right())

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
