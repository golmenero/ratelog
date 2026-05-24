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
    private val tvRatingRepository = InMemoryTvRatingRepository(tvShowRepository)
    private val handler = TopTvShowHandler(tvShowRepository, tvRatingRepository)

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
        tvRatingRepository.clear()
    }

    @Test
    fun `no filters returns ratings with shows ordered by score`() {
        val show1 = tvShowRepository.save(aTvShow(tmdbId = 100, name = "Show1"))
        val show2 = tvShowRepository.save(aTvShow(tmdbId = 200, name = "Show2"))
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
        assertEquals(1, result[0].rating.rank)
        assertEquals(2, result[1].rating.rank)
        assertEquals("Show1", result[0].show.name)
    }

    @Test
    fun `filters by category keeps absolute rank`() {
        val show1 = tvShowRepository.save(aTvShow(tmdbId = 100, name = "DramaShow", genres = "Drama"))
        val show2 = tvShowRepository.save(aTvShow(tmdbId = 200, name = "ComedyShow", genres = "Comedy"))
        val show3 = tvShowRepository.save(aTvShow(tmdbId = 300, name = "AnotherDrama", genres = "Drama"))
        tvRatingRepository.save(
            TvRating(
                tvShowId = show1.id!!,
                userId = 1,
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        tvRatingRepository.save(
            TvRating(
                tvShowId = show2.id!!,
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
                tvShowId = show3.id!!,
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

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank)
        assertEquals(3, result[1].rating.rank)
    }

    @Test
    fun `filters by name keeps absolute rank`() {
        val show = tvShowRepository.save(aTvShow(tmdbId = 100, name = "Breaking Bad"))
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

        val result = handler.handle(TopTvShow(UserId(1), null, 10, "Breaking"))

        assertEquals(1, result.size)
        assertEquals(1, result[0].rating.rank)
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
