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
        // given
        val query = TvShowPremieresQuery(User.Id(1), Lang.en)

        // when
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertTrue(premieres.items.isEmpty())
            }
        )
    }

    @Test
    fun `should return all followed shows in a single list`() {
        // given
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "Released Show", lastSeasonAirDate = LocalDate.now().minusDays(1))
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 456, originalName = "Upcoming Show", lastSeasonAirDate = LocalDate.now().plusDays(30))
        val show3 = TvShowFactory.aTvShow(id = 3, tmdbId = 789, originalName = "No Date Show", lastSeasonAirDate = null)
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)
        tvShowRepository.save(show3)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null, TmdbId(123), Lang.en, Title("Released Show"), null),
            TvDescription(null, TmdbId(456), Lang.en, Title("Upcoming Show"), null),
            TvDescription(null, TmdbId(789), Lang.en, Title("No Date Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(2))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(3))

        // when
        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(3, premieres.items.size)
            }
        )
    }

    @Test
    fun `should sort released first then upcoming then no date`() {
        // given
        val noDateShow = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "No Date Show", lastSeasonAirDate = null)
        val upcomingShow = TvShowFactory.aTvShow(id = 2, tmdbId = 456, originalName = "Upcoming Show", lastSeasonAirDate = LocalDate.now().plusDays(30))
        val releasedShow = TvShowFactory.aTvShow(id = 3, tmdbId = 789, originalName = "Released Show", lastSeasonAirDate = LocalDate.now().minusDays(1))
        tvShowRepository.save(noDateShow)
        tvShowRepository.save(upcomingShow)
        tvShowRepository.save(releasedShow)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null, TmdbId(123), Lang.en, Title("No Date Show"), null),
            TvDescription(null, TmdbId(456), Lang.en, Title("Upcoming Show"), null),
            TvDescription(null, TmdbId(789), Lang.en, Title("Released Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(2))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(3))

        // when
        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals("Released Show", premieres.items[0].name)
                assertTrue(premieres.items[0].isReleased)
                assertEquals("Upcoming Show", premieres.items[1].name)
                assertFalse(premieres.items[1].isReleased)
                assertTrue(premieres.items[1].hasDate)
                assertEquals("No Date Show", premieres.items[2].name)
                assertFalse(premieres.items[2].hasDate)
            }
        )
    }

    @Test
    fun `should mark shows with no date correctly`() {
        // given
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "No Date Show", lastSeasonAirDate = null)
        tvShowRepository.save(show)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null, TmdbId(123), Lang.en, Title("No Date Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))

        // when
        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.items.size)
                assertEquals("No Date Show", premieres.items[0].name)
                assertFalse(premieres.items[0].hasDate)
            }
        )
    }

    @Test
    fun `should use latest season for premiere date`() {
        // given
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, originalName = "Multi Season Show", lastSeasonNumber = 3, lastSeasonAirDate = LocalDate.now().minusDays(1))
        tvShowRepository.save(show)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null, TmdbId(123), Lang.en, Title("Multi Season Show"), null),
        ))
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))

        // when
        val query = TvShowPremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.items.size)
                assertEquals(3, premieres.items[0].seasonNumber)
            }
        )
    }
}
