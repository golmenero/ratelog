package org.raterr.tvrating

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.tmdb.TmdbError
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvrating.add.AddTvRating
import org.raterr.tvrating.add.AddTvRatingHandler
import org.raterr.tvrating.add.AddTvRatingHandlerError
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.get.GetTvShowHandler
import org.raterr.tvshow.get.GetTvShowHandlerError
import kotlin.test.Test

class AddTvRatingHandlerTest {

    private val getTvShowHandler: GetTvShowHandler = mock()
    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val followRepository = InMemoryFollowRepository()
    private val tvShowRepository = InMemoryTvShowRepository()
    private val handler = AddTvRatingHandler(getTvShowHandler, tvRatingRepository, followRepository)

    @BeforeEach
    fun setUp() {
        tvRatingRepository.clear()
        followRepository.clear()
        tvShowRepository.clear()
    }

    @Test
    fun `happy path returns Right and saves rating`() {
        val show = TvShow(
            id = 1,
            tmdbId = 200,
            name = "Show",
            originalName = null,
            overview = null,
            firstAirDate = "2024-01-01",
            firstAirYear = 2024,
            posterPath = null,
            tmdbVoteAverage = 7.0,
            genres = "Drama"
        )
        whenever(getTvShowHandler.handle(any())).thenReturn(show.right())

        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 6.0,
                acting = 7.0,
                soundtrack = 8.0,
                screenplay = 9.0
            )
        )

        assertTrue(result.isRight())
        assertTrue(tvRatingRepository.findAll().any())
    }

    @Test
    fun `directing below 1 returns InvalidRatingValue`() {
        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 0.9,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is AddTvRatingHandlerError.InvalidRatingValue)
    }

    @Test
    fun `cinematography above 10 returns InvalidRatingValue`() {
        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 10.1,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is AddTvRatingHandlerError.InvalidRatingValue)
    }

    @Test
    fun `acting at 0 returns InvalidRatingValue`() {
        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 0.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is AddTvRatingHandlerError.InvalidRatingValue)
    }

    @Test
    fun `all values at 1_0 is valid`() {
        val show = TvShow(id = 1, tmdbId = 200, name = "Show")
        whenever(getTvShowHandler.handle(any())).thenReturn(show.right())

        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 1.0,
                cinematography = 1.0,
                acting = 1.0,
                soundtrack = 1.0,
                screenplay = 1.0
            )
        )

        assertTrue(result.isRight())
    }

    @Test
    fun `all values at 10_0 is valid`() {
        val show = TvShow(id = 1, tmdbId = 200, name = "Show")
        whenever(getTvShowHandler.handle(any())).thenReturn(show.right())

        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0
            )
        )

        assertTrue(result.isRight())
    }

    @Test
    fun `existing rating returns RatingAlreadyExists`() {
        val show = TvShow(id = 1, tmdbId = 200, name = "Show")
        whenever(getTvShowHandler.handle(any())).thenReturn(show.right())
        tvRatingRepository.save(
            TvRating(
                id = 1,
                tvShowId = 1,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is AddTvRatingHandlerError.RatingAlreadyExists)
    }

    @Test
    fun `tvshow not found returns TvShowNotFound`() {
        whenever(getTvShowHandler.handle(any())).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is AddTvRatingHandlerError.TvShowNotFound)
    }

    @Test
    fun `auto-unfollows tvshow after rating`() {
        val show = TvShow(id = 1, tmdbId = 200, name = "Show")
        whenever(getTvShowHandler.handle(any())).thenReturn(show.right())
        val existingFollow = followRepository.save(
            Follow(
                userId = 1,
                contentType = "tvshow",
                contentTmdbId = 200,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        handler.handle(
            AddTvRating(
                tmdbId = TmdbId(200),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(followRepository.findById(existingFollow.id!!).isEmpty)
    }
}
