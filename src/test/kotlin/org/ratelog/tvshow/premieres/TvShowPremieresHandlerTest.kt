package org.ratelog.tvshow.premieres

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.TvShowFactory
import org.ratelog.user.User

class TvShowPremieresHandlerTest {
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var handler: TvShowPremieresHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        handler = TvShowPremieresHandler(tvShowRepository)
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
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Released Show")
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 456, name = "Upcoming Show")
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)

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
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "No Date Show")
        tvShowRepository.save(show)

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
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Multi Season Show")
        tvShowRepository.save(show)

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
