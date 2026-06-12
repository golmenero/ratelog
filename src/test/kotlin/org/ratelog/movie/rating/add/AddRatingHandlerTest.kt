package org.ratelog.movie.rating.add

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.movie.rating.Rating
import org.ratelog.test.InMemoryRatingRepository
import org.ratelog.test.RatingFactory
import org.ratelog.user.User
import java.time.Instant

class AddRatingHandlerTest {

    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var handler: AddRatingHandler

    @BeforeEach
    fun setUp() {
        ratingRepository = InMemoryRatingRepository()
        handler = AddRatingHandler(ratingRepository)
    }

    @Test
    fun `should add rating successfully when all values are valid`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 6.0,
            acting = 7.0,
            soundtrack = 8.0,
            screenplay = 9.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedRating = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(savedRating)
        assertEquals(7.0, savedRating!!.score!!.value)
    }

    @Test
    fun `should return InvalidRatingValue when directing is out of range`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 0.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(AddRatingHandlerError.InvalidRatingValue, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return InvalidRatingValue when cinematography is out of range`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 11.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return InvalidRatingValue when acting is out of range`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 0.5,
            soundtrack = 5.0,
            screenplay = 5.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return InvalidRatingValue when soundtrack is out of range`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 15.0,
            screenplay = 5.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return InvalidRatingValue when screenplay is out of range`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = -1.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return RatingAlreadyExists when rating already exists for movie and user`() {
        val existingRating = RatingFactory.aRating(
            id = 1,
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAt = Instant.now(),
            review = null
        )
        ratingRepository.save(existingRating)

        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 7.0,
            cinematography = 7.0,
            acting = 7.0,
            soundtrack = 7.0,
            screenplay = 7.0,
            review = null
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(AddRatingHandlerError.RatingAlreadyExists, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should add rating with review when review is provided`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 6.0,
            acting = 7.0,
            soundtrack = 8.0,
            screenplay = 9.0,
            review = "Great movie!"
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedRating = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(savedRating)
        assertEquals("Great movie!", savedRating!!.review!!.value)
    }

    @Test
    fun `should sanitize review by removing HTML tags`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            review = "<script>alert('xss')</script>Good movie"
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedRating = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(savedRating)
        assertEquals("alert('xss')Good movie", savedRating!!.review!!.value)
    }

    @Test
    fun `should not save review when review is blank`() {
        val command = AddRating(
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            review = "   "
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedRating = ratingRepository.findByMovieIdAndUserId(Movie.Id(1), User.Id(1))
        assertNotNull(savedRating)
        assertNull(savedRating!!.review)
    }
}
