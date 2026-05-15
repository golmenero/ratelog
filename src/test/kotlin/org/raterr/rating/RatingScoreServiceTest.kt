package org.raterr.rating

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class RatingScoreServiceTest {

    @Test
    fun `score with all 5`() {
        val rating = Rating(
            id = 1,
            movieId = 1,
            userId = 1,
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAtEpochMs = System.currentTimeMillis()
        )
        assertEquals(5.0, RatingScoreService.score(rating))
    }

    @Test
    fun `score with all 10`() {
        val rating = Rating(
            id = 1,
            movieId = 1,
            userId = 1,
            directing = 10.0,
            cinematography = 10.0,
            acting = 10.0,
            soundtrack = 10.0,
            screenplay = 10.0,
            createdAtEpochMs = System.currentTimeMillis()
        )
        assertEquals(10.0, RatingScoreService.score(rating))
    }

    @Test
    fun `score with all 1`() {
        val rating = Rating(
            id = 1,
            movieId = 1,
            userId = 1,
            directing = 1.0,
            cinematography = 1.0,
            acting = 1.0,
            soundtrack = 1.0,
            screenplay = 1.0,
            createdAtEpochMs = System.currentTimeMillis()
        )
        assertEquals(1.0, RatingScoreService.score(rating))
    }

    @Test
    fun `score with mixed values`() {
        val rating = Rating(
            id = 1,
            movieId = 1,
            userId = 1,
            directing = 1.0,
            cinematography = 2.0,
            acting = 3.0,
            soundtrack = 4.0,
            screenplay = 5.0,
            createdAtEpochMs = System.currentTimeMillis()
        )
        assertEquals(3.0, RatingScoreService.score(rating))
    }
}
