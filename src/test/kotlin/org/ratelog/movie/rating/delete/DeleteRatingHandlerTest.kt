package org.ratelog.movie.rating.delete

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.InMemoryRatingRepository
import org.ratelog.test.MovieFactory
import org.ratelog.test.RatingFactory
import org.ratelog.user.User
import java.time.Instant

class DeleteRatingHandlerTest {

    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var handler: DeleteRatingHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        ratingRepository = InMemoryRatingRepository()
        handler = DeleteRatingHandler(movieRepository, ratingRepository)
    }

    @Test
    fun `should delete rating successfully when movie and rating exist`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Test Movie")
        movieRepository.save(movie)

        val rating = RatingFactory.aRating(
            id = 1,
            movieId = Movie.Id(1),
            userId = User.Id(1),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAt = Instant.now()
        )
        ratingRepository.save(rating)

        val command = DeleteRating(Movie.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertNull(ratingRepository.findByMovieIdAndUserId(Movie.Id(1), command.userId))
    }

    @Test
    fun `should return MovieNotFound when movie does not exist`() {
        val command = DeleteRating(Movie.Id(999), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(DeleteRatingHandlerError.MovieNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return RatingNotFound when rating does not exist`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, title = "Test Movie")
        movieRepository.save(movie)

        val command = DeleteRating(Movie.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(DeleteRatingHandlerError.RatingNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
