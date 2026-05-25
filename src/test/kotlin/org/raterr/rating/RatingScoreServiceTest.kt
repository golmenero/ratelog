package org.raterr.rating

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.raterr.Score
import org.raterr.movie.Movie
import org.raterr.user.User
import java.time.Instant

class RatingScoreServiceTest {

    @Test
    fun `score with all 5`() {
        val rating = Rating(
            id = Rating.Id(1),
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = Score(5.0),
            cinematography = Score(5.0),
            acting = Score(5.0),
            soundtrack = Score(5.0),
            screenplay = Score(5.0),
            createdAt = Instant.now(),
            rank = Rating.Rank(0)
        )
        assertEquals(5.0, RatingScoreService.score(rating))
    }

    @Test
    fun `score with all 10`() {
        val rating = Rating(
            id = Rating.Id(1),
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = Score(10.0),
            cinematography = Score(10.0),
            acting = Score(10.0),
            soundtrack = Score(10.0),
            screenplay = Score(10.0),
            createdAt = Instant.now(),
            rank = Rating.Rank(0)
        )
        assertEquals(10.0, RatingScoreService.score(rating))
    }

    @Test
    fun `score with all 1`() {
        val rating = Rating(
            id = Rating.Id(1),
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = Score(1.0),
            cinematography = Score(1.0),
            acting = Score(1.0),
            soundtrack = Score(1.0),
            screenplay = Score(1.0),
            createdAt = Instant.now(),
            rank = Rating.Rank(0)
        )
        assertEquals(1.0, RatingScoreService.score(rating))
    }

    @Test
    fun `score with mixed values`() {
        val rating = Rating(
            id = Rating.Id(1),
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = Score(1.0),
            cinematography = Score(2.0),
            acting = Score(3.0),
            soundtrack = Score(4.0),
            screenplay = Score(5.0),
            createdAt = Instant.now(),
            rank = Rating.Rank(0)
        )
        assertEquals(3.0, RatingScoreService.score(rating))
    }
}
