package org.raterr.user.feed

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.movie.Movie
import org.raterr.movie.aMovie
import org.raterr.rating.InMemoryRatingRepository
import org.raterr.rating.Rating
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvrating.TvRating
import org.raterr.tvshow.aTvShow
import org.raterr.userfollow.InMemoryUserFollowRepository
import org.raterr.userfollow.UserFollow
import java.time.Instant

class FeedHandlerTest {

    private val userFollowRepository = InMemoryUserFollowRepository()
    private val ratingRepository = InMemoryRatingRepository()
    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val movieRepository = InMemoryMovieRepository()
    private val tvShowRepository = InMemoryTvShowRepository()
    private val handler = FeedHandler(
        userFollowRepository,
        ratingRepository,
        tvRatingRepository,
        movieRepository,
        tvShowRepository
    )

    private val now = Instant.now().toEpochMilli()

    @BeforeEach
    fun setUp() {
        userFollowRepository.clear()
        ratingRepository.clear()
        tvRatingRepository.clear()
        movieRepository.clear()
        tvShowRepository.clear()
    }

    @Test
    fun `returns empty list when user follows nobody`() {
        val result = handler.handle(FeedQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertTrue(it.isEmpty()) }
        )
    }

    @Test
    fun `returns feed items from followed users movie ratings`() {
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))
        ratingRepository.addUser(2, "bob")

        val movie = movieRepository.save(aMovie(tmdbId = 100, title = "Test Movie", posterPath = "/poster.jpg"))
        ratingRepository.save(
            Rating(movieId = movie.id!!.value, userId = 2, directing = 8.0, cinematography = 7.0, acting = 9.0, soundtrack = 6.0, screenplay = 8.0, createdAtEpochMs = now)
        )

        val result = handler.handle(FeedQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals("bob", it[0].username)
                assertEquals("Test Movie", it[0].title)
                assertEquals("movie", it[0].type)
            }
        )
    }

    @Test
    fun `returns feed items from followed users tv ratings`() {
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))
        tvRatingRepository.addUser(2, "bob")

        val show = tvShowRepository.save(aTvShow(tmdbId = 200, name = "Test Show", posterPath = "/poster.jpg"))
        tvRatingRepository.save(
            TvRating(tvShowId = show.id!!, userId = 2, directing = 8.0, cinematography = 7.0, acting = 9.0, soundtrack = 6.0, screenplay = 8.0, createdAtEpochMs = now)
        )

        val result = handler.handle(FeedQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals("bob", it[0].username)
                assertEquals("Test Show", it[0].title)
                assertEquals("tvshow", it[0].type)
            }
        )
    }

    @Test
    fun `excludes ratings older than 30 days`() {
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))
        ratingRepository.addUser(2, "bob")

        val oldDate = Instant.now().minusSeconds(31L * 24 * 60 * 60).toEpochMilli()
        val movie = movieRepository.save(aMovie(tmdbId = 100, title = "Old Movie"))
        ratingRepository.save(
            Rating(movieId = movie.id!!.value, userId = 2, directing = 8.0, cinematography = 7.0, acting = 9.0, soundtrack = 6.0, screenplay = 8.0, createdAtEpochMs = oldDate)
        )

        val result = handler.handle(FeedQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            { assertTrue(it.isEmpty()) }
        )
    }

    @Test
    fun `returns items sorted by date descending`() {
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.addUser(3, "alice")
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 3))
        ratingRepository.addUser(2, "bob")
        ratingRepository.addUser(3, "alice")

        val older = now - 100000
        val movie1 = movieRepository.save(aMovie(id = Movie.Id(1), tmdbId = 100, title = "Older Movie"))
        ratingRepository.save(
            Rating(movieId = movie1.id!!.value, userId = 2, directing = 8.0, cinematography = 7.0, acting = 9.0, soundtrack = 6.0, screenplay = 8.0, createdAtEpochMs = older)
        )

        val movie2 = movieRepository.save(aMovie(id = Movie.Id(2), tmdbId = 101, title = "Newer Movie"))
        ratingRepository.save(
            Rating(movieId = movie2.id!!.value, userId = 3, directing = 7.0, cinematography = 8.0, acting = 7.0, soundtrack = 7.0, screenplay = 7.0, createdAtEpochMs = now)
        )

        val result = handler.handle(FeedQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(2, it.size)
                assertEquals("Newer Movie", it[0].title)
                assertEquals("Older Movie", it[1].title)
            }
        )
    }
}
