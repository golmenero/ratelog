package org.ratelog.tvshow.premieres

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.*
import org.ratelog.test.InMemoryTvDescriptionRepository
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvDescription
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.LocalDate

class TvShowPremieresHandlerTest {
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var tvDescriptionRepository: InMemoryTvDescriptionRepository
    private lateinit var handler: TvShowPremieresHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        tvDescriptionRepository = InMemoryTvDescriptionRepository()
        handler = TvShowPremieresHandler(tvShowRepository, tvDescriptionRepository)
    }

    @Test
    fun `should return empty premieres when no followed shows`() {
        val query = TvShowPremieresQuery(User.Id(1), Lang.en)

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
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "Released Show", lastSeasonAirDate = LocalDate.now().minusDays(1))
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 456, originalName = "Upcoming Show", lastSeasonAirDate = LocalDate.now().plusDays(30))
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null,TmdbId(123), Lang.en, Title("Released Show"), null),
            TvDescription(null,TmdbId(456), Lang.en, Title("Upcoming Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(2))

        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
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
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "No Date Show", lastSeasonAirDate = null)
        tvShowRepository.save(show)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null,TmdbId(123), Lang.en, Title("No Date Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))

        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
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
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "Multi Season Show", lastSeasonNumber = 3, lastSeasonAirDate = LocalDate.now().minusDays(1))
        tvShowRepository.save(show)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null,TmdbId(123), Lang.en, Title("Multi Season Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))

        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
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
