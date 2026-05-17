package org.raterr.tvrating

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.tmdb.FakeTmdbClient
import org.raterr.tmdb.TmdbTvShow
import org.raterr.tvrating.add.AddTvRating
import org.raterr.tvrating.add.AddTvRatingHandler
import org.raterr.tvrating.add.AddTvRatingHandlerError
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvshow.get.GetTvShowHandler

class AddTvRatingHandlerTest {

    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val followRepository = InMemoryFollowRepository()
    private val tvShowRepository = InMemoryTvShowRepository()
    private lateinit var getTvShowHandler: GetTvShowHandler
    private lateinit var handler: AddTvRatingHandler

    @BeforeEach
    fun setUp() {
        tvRatingRepository.clear()
        followRepository.clear()
        tvShowRepository.clear()
    }

    private fun setupShow(tmdbId: Int) {
        val tmdbClient = FakeTmdbClient(tvShows = mapOf(tmdbId to TmdbTvShow(id = tmdbId, name = "Show", firstAirDate = "2024-01-01")))
        getTvShowHandler = GetTvShowHandler(tmdbClient, tvShowRepository)
        handler = AddTvRatingHandler(getTvShowHandler, tvRatingRepository, followRepository)
    }

    @Test
    fun `happy path returns Right and saves rating`() {
        setupShow(200)

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
        setupShow(200)

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
        result.fold(
            { assertTrue(it is AddTvRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `cinematography above 10 returns InvalidRatingValue`() {
        setupShow(200)

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
        result.fold(
            { assertTrue(it is AddTvRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `acting at 0 returns InvalidRatingValue`() {
        setupShow(200)

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
        result.fold(
            { assertTrue(it is AddTvRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `all values at 1_0 is valid`() {
        setupShow(200)

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
        setupShow(200)

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
        setupShow(200)
        tvShowRepository.save(org.raterr.tvshow.TvShow(id = 1, tmdbId = 200, name = "Show"))
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
        result.fold(
            { assertTrue(it is AddTvRatingHandlerError.RatingAlreadyExists) },
            { }
        )
    }

    @Test
    fun `tvshow not found returns TvShowNotFound`() {
        val tmdbClient = FakeTmdbClient()
        getTvShowHandler = GetTvShowHandler(tmdbClient, tvShowRepository)
        handler = AddTvRatingHandler(getTvShowHandler, tvRatingRepository, followRepository)

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
        result.fold(
            { assertTrue(it is AddTvRatingHandlerError.TvShowNotFound) },
            { }
        )
    }

    @Test
    fun `auto-unfollows tvshow after rating`() {
        setupShow(200)
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
