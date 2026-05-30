package org.ratelog.tvshow.rating.addseason

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.SeasonNumber
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.TvRatingFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User

class AddSeasonRatingHandlerTest {

    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: AddSeasonRatingHandler

    @BeforeEach
    fun setUp() {
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = AddSeasonRatingHandler(tvRatingRepository)
    }

    @Test
    fun `should add season rating successfully when all values are valid`() {
        val command = AddSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = SeasonNumber(1),
            userId = User.Id(1),
            directing = org.ratelog.Score(5.0),
            cinematography = org.ratelog.Score(6.0),
            acting = org.ratelog.Score(7.0),
            soundtrack = org.ratelog.Score(8.0),
            screenplay = org.ratelog.Score(9.0)
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedRating = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertNotNull(savedRating)
        assertEquals(1, savedRating!!.seasonRatings.size)
        assertEquals(7.0, savedRating.score!!.value)
    }

    @Test
    fun `should add multiple season ratings to same show`() {
        val command1 = AddSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = SeasonNumber(1),
            userId = User.Id(1),
            directing = org.ratelog.Score(5.0),
            cinematography = org.ratelog.Score(5.0),
            acting = org.ratelog.Score(5.0),
            soundtrack = org.ratelog.Score(5.0),
            screenplay = org.ratelog.Score(5.0)
        )
        handler.handle(command1)

        val command2 = AddSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = SeasonNumber(2),
            userId = User.Id(1),
            directing = org.ratelog.Score(8.0),
            cinematography = org.ratelog.Score(8.0),
            acting = org.ratelog.Score(8.0),
            soundtrack = org.ratelog.Score(8.0),
            screenplay = org.ratelog.Score(8.0)
        )

        val result = handler.handle(command2)

        assertTrue(result.isRight())
        val savedRating = tvRatingRepository.findByTvShowIdAndUserId(TvShow.Id(1), User.Id(1))
        assertEquals(2, savedRating!!.seasonRatings.size)
        assertEquals(6.5, savedRating.score!!.value)
    }
}
