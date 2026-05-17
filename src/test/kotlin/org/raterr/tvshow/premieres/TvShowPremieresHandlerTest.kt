package org.raterr.tvshow.premieres

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.tmdb.FakeTmdbClient
import org.raterr.tmdb.TmdbTvShow
import java.time.LocalDate

class TvShowPremieresHandlerTest {

    private val followRepository = InMemoryFollowRepository()
    private lateinit var tmdbClient: FakeTmdbClient
    private lateinit var handler: TvShowPremieresHandler

    private val today = LocalDate.now()

    @BeforeEach
    fun setUp() {
        followRepository.clear()
    }

    @Test
    fun `returns empty premieres when no follows`() {
        tmdbClient = FakeTmdbClient()
        handler = TvShowPremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(TvShowPremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertTrue(it.released.isEmpty())
                assertTrue(it.upcoming.isEmpty())
                assertTrue(it.noDate.isEmpty())
            }
        )
    }

    @Test
    fun `returns released shows when first air date is in the past`() {
        val pastDate = today.minusDays(5).toString()
        tmdbClient = FakeTmdbClient(tvShows = mapOf(
            200 to TmdbTvShow(id = 200, name = "Past Show", firstAirDate = pastDate, posterPath = "/poster.jpg")
        ))
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 200, createdAtEpochMs = System.currentTimeMillis()))
        handler = TvShowPremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(TvShowPremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.released.size)
                assertEquals("Past Show", it.released[0].name)
                assertTrue(it.upcoming.isEmpty())
            }
        )
    }

    @Test
    fun `returns upcoming shows when first air date is in the future`() {
        val futureDate = today.plusDays(30).toString()
        tmdbClient = FakeTmdbClient(tvShows = mapOf(
            201 to TmdbTvShow(id = 201, name = "Future Show", firstAirDate = futureDate)
        ))
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 201, createdAtEpochMs = System.currentTimeMillis()))
        handler = TvShowPremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(TvShowPremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.upcoming.size)
                assertEquals("Future Show", it.upcoming[0].name)
                assertTrue(it.released.isEmpty())
            }
        )
    }

    @Test
    fun `returns noDate shows when first air date is null`() {
        tmdbClient = FakeTmdbClient(tvShows = mapOf(
            202 to TmdbTvShow(id = 202, name = "TBA Show", firstAirDate = null)
        ))
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 202, createdAtEpochMs = System.currentTimeMillis()))
        handler = TvShowPremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(TvShowPremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.noDate.size)
                assertEquals("TBA Show", it.noDate[0].name)
                assertEquals(false, it.noDate[0].hasDate)
            }
        )
    }

    @Test
    fun `filters out movie follows`() {
        tmdbClient = FakeTmdbClient()
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 100, createdAtEpochMs = System.currentTimeMillis()))
        handler = TvShowPremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(TvShowPremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertTrue(it.released.isEmpty())
                assertTrue(it.upcoming.isEmpty())
                assertTrue(it.noDate.isEmpty())
            }
        )
    }
}
