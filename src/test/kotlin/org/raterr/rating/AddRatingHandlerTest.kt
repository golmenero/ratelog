package org.raterr.rating

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.movie.aMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.rating.add.AddRating
import org.raterr.rating.add.AddRatingHandler
import org.raterr.rating.add.AddRatingHandlerError
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbError
import org.raterr.tmdb.TmdbMovie

class AddRatingHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val ratingRepository = InMemoryRatingRepository()
    private val ratingRankService = RatingRankService(ratingRepository)
    private val followRepository = InMemoryFollowRepository()
    private val movieRepository = InMemoryMovieRepository()
    private lateinit var getMovieHandler: GetMovieHandler
    private lateinit var handler: AddRatingHandler

    @BeforeEach
    fun setUp() {
        ratingRepository.clear()
        followRepository.clear()
        movieRepository.clear()
    }

    private fun setupMovie(tmdbId: Int) {
        whenever(tmdbClient.movieDetails(tmdbId)).thenReturn(TmdbMovie(id = tmdbId, title = "Movie", releaseDate = "2024-01-01").right())
        getMovieHandler = GetMovieHandler(tmdbClient, movieRepository)
        handler = AddRatingHandler(getMovieHandler, ratingRepository, followRepository, ratingRankService)
    }

    @Test
    fun `happy path returns Right and saves rating`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 6.0,
                acting = 7.0,
                soundtrack = 8.0,
                screenplay = 9.0
            )
        )

        assertTrue(result.isRight())
        assertTrue(ratingRepository.findAll().any())
    }

    @Test
    fun `directing below 1 returns InvalidRatingValue`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 0.9,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `directing above 10 returns InvalidRatingValue`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 10.1,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `cinematography at 0 returns InvalidRatingValue`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 0.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `acting above 10 returns InvalidRatingValue`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 11.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `soundtrack negative returns InvalidRatingValue`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = -1.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `screenplay at 0_5 returns InvalidRatingValue`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 0.5
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.InvalidRatingValue) },
            { }
        )
    }

    @Test
    fun `all values at 1_0 is valid`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 1.0,
                cinematography = 1.0,
                acting = 1.0,
                soundtrack = 1.0,
                screenplay = 1.0
            )
        )

        assertTrue(result.isRight())
    }

    @Test
    fun `all values at 10_0 is valid`() {
        setupMovie(100)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0
            )
        )

        assertTrue(result.isRight())
    }

    @Test
    fun `existing rating returns RatingAlreadyExists`() {
        setupMovie(100)
        movieRepository.save(aMovie(id = 1, tmdbId = 100, title = "Movie"))
        ratingRepository.save(
            Rating(
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
        )

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.RatingAlreadyExists) },
            { }
        )
    }

    @Test
    fun `movie not found returns MovieNotFound`() {
        whenever(tmdbClient.movieDetails(100)).thenReturn(TmdbError.MovieNotFound.left())
        getMovieHandler = GetMovieHandler(tmdbClient, movieRepository)
        handler = AddRatingHandler(getMovieHandler, ratingRepository, followRepository, ratingRankService)

        val result = handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is AddRatingHandlerError.MovieNotFound) },
            { }
        )
    }

    @Test
    fun `auto-unfollows movie after rating`() {
        setupMovie(100)
        val existingFollow = followRepository.save(
            Follow(
                userId = 1,
                contentType = "movie",
                contentTmdbId = 100,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        handler.handle(
            AddRating(
                tmdbId = TmdbId(100),
                userId = UserId(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        assertTrue(followRepository.findById(existingFollow.id!!).isEmpty)
    }
}
