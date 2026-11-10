package org.ratelog.tvshow.rating.updateseason

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.SeasonNumber
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.TvRatingFactory
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.Instant

class UpdateSeasonRatingHandlerTest {

    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: UpdateSeasonRatingHandler

    @BeforeEach
    fun setUp() {
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = UpdateSeasonRatingHandler(tvRatingRepository)
    }

    private fun givenRatingWithSeasons(): InMemoryTvRatingRepository {
        tvRatingRepository.save(
            TvRatingFactory.aTvRating(
                id = 1,
                tvShowId = TvShow.Id(1),
                userId = User.Id(1),
                seasonRatings = listOf(
                    TvRatingFactory.aSeasonRating(
                        id = 1,
                        tvShowId = TvShow.Id(1),
                        seasonNumber = 1,
                        userId = User.Id(1),
                        directing = 5.0,
                        cinematography = 5.0,
                        acting = 5.0,
                        soundtrack = 5.0,
                        screenplay = 5.0,
                        createdAt = Instant.now(),
                        reviewTitle = null, reviewText = null
                    ),
                    TvRatingFactory.aSeasonRating(
                        id = 2,
                        tvShowId = TvShow.Id(1),
                        seasonNumber = 2,
                        userId = User.Id(1),
                        directing = 6.0,
                        cinematography = 6.0,
                        acting = 6.0,
                        soundtrack = 6.0,
                        screenplay = 6.0,
                        createdAt = Instant.now(),
                        reviewTitle = null, reviewText = null
                    )
                )
            )
        )
        return tvRatingRepository
    }

    @Test
    fun `given a rated season when updating it then the scores and the overall score are updated`() {
        // Given
        givenRatingWithSeasons()

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 6.0,
                cinematography = 7.0,
                acting = 8.0,
                soundtrack = 9.0,
                screenplay = 10.0,
                reviewTitle = null, reviewText = null
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updated)
        val season = updated!!.seasonRatings.first { it.seasonNumber == SeasonNumber(1) }
        assertEquals(6.0, season.directing.value)
        assertEquals(7.0, season.cinematography.value)
        assertEquals(8.0, season.acting.value)
        assertEquals(9.0, season.soundtrack.value)
        assertEquals(10.0, season.screenplay.value)
        assertEquals(8.0, season.score.value)
        assertEquals(7.0, updated.score!!.value)
    }

    @Test
    fun `given a rated season when updating it then the other seasons are kept untouched`() {
        // Given
        givenRatingWithSeasons()

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0,
                reviewTitle = null, reviewText = null
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updated)
        val season = updated!!.seasonRatings.first { it.seasonNumber == SeasonNumber(2) }
        assertEquals(6.0, season.directing.value)
        assertEquals(6.0, season.score.value)
    }

    @Test
    fun `given a rated season when updating it with a review then the review is saved`() {
        // Given
        givenRatingWithSeasons()

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = "Great season",
                reviewText = "Great season!"
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updated)
        val season = updated!!.seasonRatings.first { it.seasonNumber == SeasonNumber(1) }
        assertEquals("Great season", season.review!!.title!!.value)
        assertEquals("Great season!", season.review.text!!.value)
    }

    @Test
    fun `given a rated season when updating it with a review with html then the review is sanitized`() {
        // Given
        givenRatingWithSeasons()

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null,
                reviewText = "<script>alert('xss')</script>Good season"
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updated)
        val season = updated!!.seasonRatings.first { it.seasonNumber == SeasonNumber(1) }
        assertEquals("alert('xss')Good season", season.review!!.text!!.value)
    }

    @Test
    fun `given a rated season when updating it with line breaks then more than two consecutive newlines are collapsed`() {
        givenRatingWithSeasons()

        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null,
                reviewText = "Line1\n\nLine2\n\n\nLine3"
            )
        )

        assertTrue(result.isRight())
        val updated = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updated)
        val season = updated!!.seasonRatings.first { it.seasonNumber == SeasonNumber(1) }
        assertEquals("Line1\n\nLine2\n\nLine3", season.review!!.text!!.value)
    }

    @Test
    fun `given a rated season with a review when updating it with a blank review then the review is removed`() {
        // Given
        tvRatingRepository.save(
            TvRatingFactory.aTvRating(
                id = 1,
                tvShowId = TvShow.Id(1),
                userId = User.Id(1),
                seasonRatings = listOf(
                    TvRatingFactory.aSeasonRating(
                        id = 1,
                        tvShowId = TvShow.Id(1),
                        seasonNumber = 1,
                        userId = User.Id(1),
                        createdAt = Instant.now(),
                        reviewTitle = "Old review",
                        reviewText = "Old review"
                    )
                )
            )
        )

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = "   ",
                reviewText = "   "
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(updated)
        val season = updated!!.seasonRatings.first { it.seasonNumber == SeasonNumber(1) }
        assertNull(season.review)
    }

    @Test
    fun `given a rated season when updating it with an out of range score then invalid rating value is returned`() {
        // Given
        givenRatingWithSeasons()

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = -1.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null, reviewText = null
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateSeasonRatingHandlerError.InvalidRatingValue, result.fold({ it }, { fail("Should not return success") }))
        val untouched = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        val season = untouched!!.seasonRatings.first { it.seasonNumber == SeasonNumber(1) }
        assertEquals(5.0, season.directing.value)
    }

    @Test
    fun `given no rating when updating a season then rating not found is returned`() {
        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null, reviewText = null
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateSeasonRatingHandlerError.RatingNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `given a rating without that season when updating it then season rating not found is returned`() {
        // Given
        givenRatingWithSeasons()

        // When
        val result = handler.handle(
            UpdateSeasonRating(
                tvShowId = TvShow.Id(1),
                seasonNumber = SeasonNumber(3),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null, reviewText = null
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateSeasonRatingHandlerError.SeasonRatingNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
