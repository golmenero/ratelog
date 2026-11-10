package org.ratelog.movie.rating.update

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.test.InMemoryRatingRepository
import org.ratelog.test.RatingFactory
import org.ratelog.user.User
import java.time.Instant

class UpdateRatingHandlerTest {

    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var handler: UpdateRatingHandler

    @BeforeEach
    fun setUp() {
        ratingRepository = InMemoryRatingRepository()
        handler = UpdateRatingHandler(ratingRepository)
    }

    @Test
    fun `given an existing rating when updating it then the scores and the overall score are updated`() {
        // Given
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAt = Instant.now(),
                reviewTitle = null, reviewText = null
            )
        )

        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
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
        val updated = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(updated)
        assertEquals(6.0, updated!!.directing.value)
        assertEquals(7.0, updated.cinematography.value)
        assertEquals(8.0, updated.acting.value)
        assertEquals(9.0, updated.soundtrack.value)
        assertEquals(10.0, updated.screenplay.value)
        assertEquals(8.0, updated.score!!.value)
    }

    @Test
    fun `given an existing rating when updating it with a review then the review is saved`() {
        // Given
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(1),
                createdAt = Instant.now(),
                reviewTitle = null, reviewText = null
            )
        )

        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = "Great movie",
                reviewText = "Great movie!"
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(updated)
        assertEquals("Great movie", updated!!.review!!.title!!.value)
        assertEquals("Great movie!", updated.review.text!!.value)
    }

    @Test
    fun `given an existing rating when updating it with a review with html then the review is sanitized`() {
        // Given
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(1),
                createdAt = Instant.now(),
                reviewTitle = null, reviewText = null
            )
        )

        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null,
                reviewText = "<script>alert('xss')</script>Good movie"
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(updated)
        assertEquals("alert('xss')Good movie", updated!!.review!!.text!!.value)
    }

    @Test
    fun `given an existing rating when updating it with line breaks then more than two consecutive newlines are collapsed`() {
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(1),
                createdAt = Instant.now(),
                reviewTitle = null, reviewText = null
            )
        )

        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
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
        val updated = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(updated)
        assertEquals("Line1\n\nLine2\n\nLine3", updated!!.review!!.text!!.value)
    }

    @Test
    fun `given an existing rating with a review when updating it with a blank review then the review is removed`() {
        // Given
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(1),
                createdAt = Instant.now(),
                reviewTitle = "Old review",
                reviewText = "Old review"
            )
        )

        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
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
        val updated = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(updated)
        assertNull(updated!!.review)
    }

    @Test
    fun `given an existing rating when updating it with an out of range score then invalid rating value is returned`() {
        // Given
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAt = Instant.now(),
                reviewTitle = null, reviewText = null
            )
        )

        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
                userId = User.Id(1),
                directing = 11.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                reviewTitle = null, reviewText = null
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateRatingHandlerError.InvalidRatingValue, result.fold({ it }, { fail("Should not return success") }))
        val untouched = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertEquals(5.0, untouched!!.directing.value)
    }

    @Test
    fun `given no rating when updating it then rating not found is returned`() {
        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
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
        assertEquals(UpdateRatingHandlerError.RatingNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `given a rating of another user when updating it then rating not found is returned`() {
        // Given
        ratingRepository.save(
            RatingFactory.aRating(
                id = 1,
                movieId = Movie.Id(1),
                userId = User.Id(2),
                createdAt = Instant.now(),
                reviewTitle = null, reviewText = null
            )
        )

        // When
        val result = handler.handle(
            UpdateRating(
                movieId = Movie.Id(1),
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
        assertEquals(UpdateRatingHandlerError.RatingNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
