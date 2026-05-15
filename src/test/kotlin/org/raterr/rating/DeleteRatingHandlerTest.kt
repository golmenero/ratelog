package org.raterr.rating

import arrow.core.Either
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.movie.Movie
import org.raterr.rating.delete.DeleteRating
import org.raterr.rating.delete.DeleteRatingHandler
import org.raterr.rating.delete.DeleteRatingHandlerError
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.rating.InMemoryRatingRepository
import kotlin.test.Test

class DeleteRatingHandlerTest {

    private val movieRepository = InMemoryMovieRepository()
    private val ratingRepository = InMemoryRatingRepository()
    private val handler = DeleteRatingHandler(movieRepository, ratingRepository)

    @BeforeEach
    fun setUp() {
        movieRepository.clear()
        ratingRepository.clear()
    }

    @Test
    fun `happy path returns Right`() {
        val movie = movieRepository.save(Movie(tmdbId = 100, title = "Movie"))
        ratingRepository.save(
            Rating(
                movieId = movie.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(DeleteRating(TmdbId(100), UserId(1)))

        assertTrue(result.isRight())
    }

    @Test
    fun `movie not found returns MovieNotFound`() {
        val result = handler.handle(DeleteRating(TmdbId(100), UserId(1)))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is DeleteRatingHandlerError.MovieNotFound)
    }

    @Test
    fun `rating not found returns RatingNotFound`() {
        movieRepository.save(Movie(tmdbId = 100, title = "Movie"))

        val result = handler.handle(DeleteRating(TmdbId(100), UserId(1)))

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is DeleteRatingHandlerError.RatingNotFound)
    }
}
