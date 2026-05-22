package org.raterr.movie

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.movie.top.TopMovie
import org.raterr.movie.top.TopMovieHandler
import org.raterr.rating.Rating
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.rating.InMemoryRatingRepository

class TopMovieHandlerTest {

    private val movieRepository = InMemoryMovieRepository()
    private val ratingRepository = InMemoryRatingRepository(movieRepository)
    private val handler = TopMovieHandler(movieRepository, ratingRepository)

    @BeforeEach
    fun setUp() {
        movieRepository.clear()
        ratingRepository.clear()
    }

    @Test
    fun `no filters returns ratings with movies ordered by score`() {
        val movie1 = movieRepository.save(Movie(tmdbId = 100, title = "Movie1"))
        val movie2 = movieRepository.save(Movie(tmdbId = 200, title = "Movie2"))
        ratingRepository.save(
            Rating(
                movieId = movie1.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        ratingRepository.save(
            Rating(
                movieId = movie2.id!!,
                userId = 1,
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(TopMovie(UserId(1), null, 10, null))

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank)
        assertEquals(2, result[1].rating.rank)
        assertEquals("Movie2", result[0].movie.title)
    }

    @Test
    fun `filters by category keeps absolute rank`() {
        val movie1 = movieRepository.save(Movie(tmdbId = 100, title = "ActionMovie", genres = "Action"))
        val movie2 = movieRepository.save(Movie(tmdbId = 200, title = "DramaMovie", genres = "Drama"))
        val movie3 = movieRepository.save(Movie(tmdbId = 300, title = "AnotherAction", genres = "Action"))
        ratingRepository.save(
            Rating(
                movieId = movie1.id!!,
                userId = 1,
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        ratingRepository.save(
            Rating(
                movieId = movie2.id!!,
                userId = 1,
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        ratingRepository.save(
            Rating(
                movieId = movie3.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(TopMovie(UserId(1), "Action", 10, null))

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank)
        assertEquals(3, result[1].rating.rank)
    }

    @Test
    fun `filters by name keeps absolute rank`() {
        val movie = movieRepository.save(Movie(tmdbId = 100, title = "The Matrix"))
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

        val result = handler.handle(TopMovie(UserId(1), null, 10, "Matrix"))

        assertEquals(1, result.size)
        assertEquals(1, result[0].rating.rank)
    }

    @Test
    fun `limits results`() {
        val result = handler.handle(TopMovie(UserId(1), null, 3, null))

        assertEquals(0, result.size)
    }

    @Test
    fun `empty returns empty list`() {
        val result = handler.handle(TopMovie(UserId(1), null, 10, null))

        assertEquals(0, result.size)
    }
}
