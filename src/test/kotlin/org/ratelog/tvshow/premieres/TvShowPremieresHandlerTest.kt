package org.ratelog.tvshow.premieres

import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.TmdbId
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.TmdbFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import java.time.LocalDate

class TvShowPremieresHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var handler: TvShowPremieresHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        handler = TvShowPremieresHandler(tmdbClient, tvShowRepository)
    }

    @Test
    fun `should return empty premieres when no followed shows`() {
        val query = TvShowPremieresQuery(User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertTrue(premieres.released.isEmpty())
                assertTrue(premieres.upcoming.isEmpty())
                assertTrue(premieres.noDate.isEmpty())
            }
        )
    }

    @Test
    fun `should categorize shows into released and upcoming based on latest season`() {
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Released Show", followed = true)
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 456, name = "Upcoming Show", followed = true)
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)

        val tmdbShow1 = TmdbFactory.aTmdbTvShow(
            id = 123,
            name = "Released Show",
            seasons = listOf(
                TmdbFactory.aTmdbSeason(1, 10, "2020-01-01"),
                TmdbFactory.aTmdbSeason(2, 12, "2020-06-01")
            )
        )
        val tmdbShow2 = TmdbFactory.aTmdbTvShow(
            id = 456,
            name = "Upcoming Show",
            seasons = listOf(
                TmdbFactory.aTmdbSeason(1, 10, LocalDate.now().plusDays(30).toString())
            )
        )
        whenever(tmdbClient.tvShowDetails(123)).thenReturn(tmdbShow1.right())
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(tmdbShow2.right())

        val query = TvShowPremieresQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.released.size)
                assertEquals(1, premieres.upcoming.size)
                assertEquals("Released Show", premieres.released[0].name)
                assertEquals("Upcoming Show", premieres.upcoming[0].name)
            }
        )
    }

    @Test
    fun `should categorize shows with no date into noDate list`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "No Date Show", followed = true)
        tvShowRepository.save(show)

        val tmdbShow = TmdbFactory.aTmdbTvShow(
            id = 123,
            name = "No Date Show",
            seasons = listOf(TmdbFactory.aTmdbSeason(1, 10, null))
        )
        whenever(tmdbClient.tvShowDetails(123)).thenReturn(tmdbShow.right())

        val query = TvShowPremieresQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.noDate.size)
                assertEquals("No Date Show", premieres.noDate[0].name)
                assertFalse(premieres.noDate[0].hasDate)
            }
        )
    }

    @Test
    fun `should use latest season for premiere date`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Multi Season Show", followed = true)
        tvShowRepository.save(show)

        val tmdbShow = TmdbFactory.aTmdbTvShow(
            id = 123,
            name = "Multi Season Show",
            seasons = listOf(
                TmdbFactory.aTmdbSeason(1, 10, "2020-01-01"),
                TmdbFactory.aTmdbSeason(2, 12, "2021-01-01"),
                TmdbFactory.aTmdbSeason(3, 8, "2022-01-01")
            )
        )
        whenever(tmdbClient.tvShowDetails(123)).thenReturn(tmdbShow.right())

        val query = TvShowPremieresQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.released.size)
                assertEquals(3, premieres.released[0].seasonNumber)
            }
        )
    }
}
