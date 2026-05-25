package org.raterr.tvrating

import arrow.core.Either
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.raterr.TmdbId
import org.raterr.user.User.Id
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.aTvShow
import org.raterr.tvrating.delete.DeleteTvRating
import org.raterr.tvrating.delete.DeleteTvRatingHandler
import org.raterr.tvrating.delete.DeleteTvRatingHandlerError
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
            aTvRating(
                tvShowId = show.id!!,
                userId = Id(1)
            )
        )

        val result = handler.handle(DeleteTvRating(TmdbId(200), Id(1)))

        assertTrue(result.isRight())
    }

    @Test
    fun `tvshow not found returns TvShowNotFound`() {
        val result = handler.handle(DeleteTvRating(TmdbId(200), Id(1)))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is DeleteTvRatingHandlerError.TvShowNotFound)
    }

    @Test
    fun `rating not found returns RatingNotFound`() {
        tvShowRepository.save(aTvShow(tmdbId = 200, name = "Show"))

        val result = handler.handle(DeleteTvRating(TmdbId(200), Id(1)))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is DeleteTvRatingHandlerError.RatingNotFound)
    }
}
