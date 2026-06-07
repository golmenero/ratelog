package org.ratelog.user.feed

import arrow.core.getOrElse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.test.*
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.Instant

class FeedHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: FeedHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        ratingRepository = InMemoryRatingRepository()
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = FeedHandler(userRepository, ratingRepository, tvRatingRepository)
    }

    @Test
    fun `should return empty list when user follows no one`() {
        val query = FeedQuery(User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isRight())
        assertEquals(0, result.getOrElse { emptyList() }.size)
    }

    @Test
    fun `should return feed items from followed users movie ratings`() {
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com", followed = true)
        userRepository.save(followedUser)

        val rating = RatingFactory.aRating(movieId = Movie.Id(1), userId = User.Id(2), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now())
        ratingRepository.save(rating)

        val query = FeedQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val feedItems = result.getOrElse { emptyList() }
        assertEquals(1, feedItems.size)
    }

    @Test
    fun `should return feed items from followed users tv ratings`() {
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com", followed = true)
        userRepository.save(followedUser)

        val seasonRating = TvRatingFactory.aSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = 1,
            userId = User.Id(2),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAt = Instant.now()
        )
        val tvRating = TvRatingFactory.aTvRating(
            tvShowId = TvShow.Id(1),
            userId = User.Id(2),
            seasonRatings = listOf(seasonRating),
            createdAt = Instant.now()
        )
        tvRatingRepository.save(tvRating)

        val query = FeedQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val feedItems = result.getOrElse { emptyList() }
        assertEquals(1, feedItems.size)
    }
}
