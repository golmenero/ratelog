package org.ratelog.tvshow.rating.deleteseason

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.SeasonNumber
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.TvRatingFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.Instant

class DeleteSeasonRatingHandlerTest {

    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: DeleteSeasonRatingHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = DeleteSeasonRatingHandler(tvShowRepository, tvRatingRepository)
    }

    @Test
    fun `should delete season rating successfully when show and rating exist`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Test Show")
        tvShowRepository.save(show)

        val seasonRating1 = TvRatingFactory.aSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = 1,
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAt = Instant.now(),
            review = null
        )
        val seasonRating2 = TvRatingFactory.aSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = 2,
            userId = User.Id(1),
            directing = 7.0,
            cinematography = 7.0,
            acting = 7.0,
            soundtrack = 7.0,
            screenplay = 7.0,
            createdAt = Instant.now(),
            review = null
        )
        val tvRating = TvRatingFactory.aTvRating(
            id = 1,
            tvShowId = TvShow.Id(1),
            userId = User.Id(1),
            seasonRatings = listOf(seasonRating1, seasonRating2),
            createdAt = Instant.now()
        )
        tvRatingRepository.save(tvRating)

        val command = DeleteSeasonRating(TvShow.Id(1), SeasonNumber(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedRating = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updatedRating)
        assertEquals(1, updatedRating!!.seasonRatings.size)
        assertEquals(2, updatedRating.seasonRatings[0].seasonNumber.value)
    }

    @Test
    fun `should delete tv rating completely when last season rating is removed`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Test Show")
        tvShowRepository.save(show)

        val seasonRating = TvRatingFactory.aSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = 1,
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAt = Instant.now(),
            review = null
        )
        val tvRating = TvRatingFactory.aTvRating(
            id = 1,
            tvShowId = TvShow.Id(1),
            userId = User.Id(1),
            seasonRatings = listOf(seasonRating),
            createdAt = Instant.now()
        )
        tvRatingRepository.save(tvRating)

        val command = DeleteSeasonRating(TvShow.Id(1), SeasonNumber(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertNull(tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1)))
    }

    @Test
    fun `should return TvShowNotFound when show does not exist`() {
        val command = DeleteSeasonRating(TvShow.Id(999), SeasonNumber(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(DeleteSeasonRatingHandlerError.TvShowNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return RatingNotFound when rating does not exist`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Test Show")
        tvShowRepository.save(show)

        val command = DeleteSeasonRating(TvShow.Id(1), SeasonNumber(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(DeleteSeasonRatingHandlerError.RatingNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
