package org.raterr.tvrating

import arrow.core.Either
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvrating.delete.DeleteTvRating
import org.raterr.tvrating.delete.DeleteTvRatingHandler
import org.raterr.tvrating.delete.DeleteTvRatingHandlerError
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.aTvShow
import kotlin.test.Test

class DeleteTvRatingHandlerTest {

    private val tvShowRepository = InMemoryTvShowRepository()
    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val tvRatingRankService = TvRatingRankService(tvRatingRepository)
    private val handler = DeleteTvRatingHandler(tvShowRepository, tvRatingRepository, tvRatingRankService)

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
        tvRatingRepository.clear()
    }

    @Test
    fun `happy path returns Right`() {
        val show = tvShowRepository.save(aTvShow(tmdbId = 200, name = "Show"))
        tvRatingRepository.save(
            TvRating(
                tvShowId = show.id!!.value,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(DeleteTvRating(TmdbId(200), UserId(1)))

        assertTrue(result.isRight())
    }

    @Test
    fun `tvshow not found returns TvShowNotFound`() {
        val result = handler.handle(DeleteTvRating(TmdbId(200), UserId(1)))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is DeleteTvRatingHandlerError.TvShowNotFound)
    }

    @Test
    fun `rating not found returns RatingNotFound`() {
        tvShowRepository.save(aTvShow(tmdbId = 200, name = "Show"))

        val result = handler.handle(DeleteTvRating(TmdbId(200), UserId(1)))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is DeleteTvRatingHandlerError.RatingNotFound)
    }
}
