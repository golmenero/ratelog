package org.raterr.movie

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.Genre
import org.raterr.user.User.Id
import org.raterr.movie.top.TopMovie
import org.raterr.movie.top.TopMovieHandler
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.rating.InMemoryRatingRepository
import org.raterr.rating.aRating

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
        val movie1 = movieRepository.save(aMovie(id = Movie.Id(1), tmdbId = 100, title = "Movie1"))
        val movie2 = movieRepository.save(aMovie(id = Movie.Id(2), tmdbId = 200, title = "Movie2"))
        ratingRepository.save(
            aRating(
                movieId = movie1.id!!,
                userId = Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )
        ratingRepository.save(
            aRating(
                movieId = movie2.id!!,
                userId = Id(1),
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0
            )
        )

        val result = handler.handle(TopMovie(Id(1), null, 10, null))

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank.value)
        assertEquals(2, result[1].rating.rank.value)
        assertEquals("Movie2", result[0].movie.title.value)
    }

    @Test
    fun `filters by category keeps absolute rank`() {
        val movie1 = movieRepository.save(aMovie(id = Movie.Id(1),tmdbId = 100, title = "ActionMovie", genres = listOf(Genre.Action)))
        val movie2 = movieRepository.save(aMovie(id = Movie.Id(2),tmdbId = 200, title = "DramaMovie", genres = listOf(Genre.Drama)))
        val movie3 = movieRepository.save(aMovie(id = Movie.Id(3),tmdbId = 300, title = "AnotherAction", genres = listOf(Genre.Action)))
        ratingRepository.save(
            aRating(
                movieId = movie1.id!!,
                userId = Id(1),
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0
            )
        )
        ratingRepository.save(
            aRating(
                movieId = movie2.id!!,
                userId = Id(1),
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0
            )
        )
        ratingRepository.save(
            aRating(
                movieId = movie3.id!!,
                userId = Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        val result = handler.handle(TopMovie(Id(1), "Action", 10, null))

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank.value)
        assertEquals(3, result[1].rating.rank.value)
    }

    @Test
    fun `filters by name keeps absolute rank`() {
        val movie = movieRepository.save(aMovie(tmdbId = 100, title = "The Matrix"))
        ratingRepository.save(
            aRating(
                movieId = movie.id!!,
                userId = Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        val result = handler.handle(TopMovie(Id(1), null, 10, "Matrix"))

        assertEquals(1, result.size)
        assertEquals(1, result[0].rating.rank.value)
    }

    @Test
    fun `limits results`() {
        val result = handler.handle(TopMovie(Id(1), null, 3, null))

        assertEquals(0, result.size)
    }

    @Test
    fun `empty returns empty list`() {
        val result = handler.handle(TopMovie(Id(1), null, 10, null))

        assertEquals(0, result.size)
    }
}
