package org.raterr.tvshow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvrating.TvRating
import org.raterr.tvshow.top.TopTvShow
import org.raterr.tvshow.top.TopTvShowHandler

class TopTvShowHandlerTest {

    private val tvShowRepository = InMemoryTvShowRepository()
    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val handler = TopTvShowHandler(tvShowRepository, tvRatingRepository)

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
        tvRatingRepository.clear()
    }

    @Test
    fun `no filters returns ratings with shows`() {
        val show1 = tvShowRepository.save(TvShow(tmdbId = 100, name = "Show1"))
        val show2 = tvShowRepository.save(TvShow(tmdbId = 200, name = "Show2"))
        tvRatingRepository.save(
            TvRating(
                tvShowId = show1.id!!,
                userId = 1,
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        tvRatingRepository.save(
            TvRating(
                tvShowId = show2.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(TopTvShow(UserId(1), null, 10, null))

        assertEquals(2, result.size)
        assertEquals("Show1", result[0].second.name)
        assertEquals("Show2", result[1].second.name)
    }

    @Test
    fun `filters by category`() {
        val show = tvShowRepository.save(TvShow(tmdbId = 100, name = "Show"))
        tvRatingRepository.save(
            TvRating(
                tvShowId = show.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(TopTvShow(UserId(1), "Drama", 10, null))

        assertEquals(1, result.size)
    }

    @Test
    fun `filters by name`() {
        val show = tvShowRepository.save(TvShow(tmdbId = 100, name = "Show"))
        tvRatingRepository.save(
            TvRating(
                tvShowId = show.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(TopTvShow(UserId(1), null, 10, "Test"))

        assertEquals(1, result.size)
    }

    @Test
    fun `limits results`() {
        val result = handler.handle(TopTvShow(UserId(1), null, 3, null))

        assertEquals(0, result.size)
    }

    @Test
    fun `empty returns empty list`() {
        val result = handler.handle(TopTvShow(UserId(1), null, 10, null))

        assertEquals(0, result.size)
    }
}
