package org.raterr.search

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.MediaType
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.movie.Movie
import org.raterr.rating.InMemoryRatingRepository
import org.raterr.rating.Rating
import org.raterr.tmdb.FakeTmdbClient
import org.raterr.tmdb.TmdbMovie
import org.raterr.tmdb.TmdbTvShow
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvshow.InMemoryTvShowRepository
import java.time.LocalDate

class SearchHandlerTest {

    private val tvShowRepository = InMemoryTvShowRepository()
    private val movieRepository = InMemoryMovieRepository()
    private val ratingRepository = InMemoryRatingRepository()
    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val followRepository = InMemoryFollowRepository()
    private lateinit var tmdbClient: FakeTmdbClient
    private lateinit var handler: SearchHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
        movieRepository.clear()
        ratingRepository.clear()
        tvRatingRepository.clear()
        followRepository.clear()
    }

    @Test
    fun `happy path returns interleaved results`() {
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")),
            tvShows = mapOf(2 to TmdbTvShow(id = 2, name = "Show", firstAirDate = "2024-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(2, it.size)
                assertEquals("Movie", it[0].title)
                assertEquals("Show", it[1].title)
            }
        )
    }

    @Test
    fun `only movies returns movie results`() {
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals(MediaType.movie.name, it[0].type)
            }
        )
    }

    @Test
    fun `only tvshows returns show results`() {
        tmdbClient = FakeTmdbClient(
            tvShows = mapOf(2 to TmdbTvShow(id = 2, name = "Show", firstAirDate = "2024-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals(MediaType.tvshow.name, it[0].type)
            }
        )
    }

    @Test
    fun `interleaves 3 movies and 2 shows correctly`() {
        tmdbClient = FakeTmdbClient(
            movies = mapOf(
                1 to TmdbMovie(id = 1, title = "M1", releaseDate = "2024-01-01"),
                2 to TmdbMovie(id = 2, title = "M2", releaseDate = "2024-01-01"),
                3 to TmdbMovie(id = 3, title = "M3", releaseDate = "2024-01-01")
            ),
            tvShows = mapOf(
                10 to TmdbTvShow(id = 10, name = "S1", firstAirDate = "2024-01-01"),
                11 to TmdbTvShow(id = 11, name = "S2", firstAirDate = "2024-01-01")
            )
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(5, it.size)
                assertEquals("M1", it[0].title)
                assertEquals("S1", it[1].title)
                assertEquals("M2", it[2].title)
                assertEquals("S2", it[3].title)
                assertEquals("M3", it[4].title)
            }
        )
    }

    @Test
    fun `blank query returns empty list`() {
        tmdbClient = FakeTmdbClient()
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(0, it.size) }
        )
    }

    @Test
    fun `isFollowed true when user follows movie`() {
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01"))
        )
        followRepository.save(Follow(userId = 1, contentType = MediaType.movie.name, contentTmdbId = 1, createdAtEpochMs = System.currentTimeMillis()))
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(true, it[0].isFollowed) }
        )
    }

    @Test
    fun `canRate false for future release date`() {
        val futureDate = LocalDate.now().plusYears(1).toString()
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = futureDate))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(false, it[0].canRate) }
        )
    }

    @Test
    fun `canRate true for past release date`() {
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2020-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(true, it[0].canRate) }
        )
    }

    @Test
    fun `canFollow false when rating exists`() {
        val movie = movieRepository.save(Movie(id = 5, tmdbId = 1, title = "Movie"))
        ratingRepository.save(
            Rating(
                id = 1,
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
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(false, it[0].canFollow) }
        )
    }

    @Test
    fun `canFollow true when no rating exists`() {
        movieRepository.save(Movie(id = 5, tmdbId = 1, title = "Movie"))
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(true, it[0].canFollow) }
        )
    }

    @Test
    fun `userId null means isFollowed false`() {
        tmdbClient = FakeTmdbClient(
            movies = mapOf(1 to TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01"))
        )
        handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

        val result = handler.handle(SearchQuery("test", null))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertEquals(false, it[0].isFollowed) }
        )
    }
}
