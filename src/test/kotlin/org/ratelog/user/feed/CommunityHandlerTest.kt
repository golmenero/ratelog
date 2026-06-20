package org.ratelog.user.feed

import arrow.core.getOrElse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.movie.Movie
import org.ratelog.test.*
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import org.ratelog.user.community.CommunityHandler
import org.ratelog.user.community.FeedQuery
import java.time.Instant

class CommunityHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: CommunityHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        ratingRepository = InMemoryRatingRepository(userRepository)
        tvRatingRepository = InMemoryTvRatingRepository(userRepository)
        handler = CommunityHandler(userRepository, ratingRepository, tvRatingRepository)
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
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com")
        userRepository.save(followedUser)
        userRepository.toggleFollow(User.Id(1), User.Id(2))

        val rating = RatingFactory.aRating(movieId = Movie.Id(1), userId = User.Id(2), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now(), review = null)
        ratingRepository.save(rating)

        val query = FeedQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val feedItems = result.getOrElse { emptyList() }
        assertEquals(1, feedItems.size)
        assertEquals("followeduser", feedItems[0].username)
        assertEquals("movie", feedItems[0].title)
        assertNull(feedItems[0].reviewText)
        assertNull(feedItems[0].seasonNumber)
    }

    @Test
    fun `should return feed items from followed users tv ratings`() {
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com")
        userRepository.save(followedUser)
        userRepository.toggleFollow(User.Id(1), User.Id(2))

        val seasonRating = TvRatingFactory.aSeasonRating(
            tvShowId = TvShow.Id(1),
            seasonNumber = 1,
            userId = User.Id(2),
            directing = 5.0,
            cinematography = 5.0,
            acting = 5.0,
            soundtrack = 5.0,
            screenplay = 5.0,
            createdAt = Instant.now(),
            review = null
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
        assertEquals("followeduser", feedItems[0].username)
        assertEquals("tvshow", feedItems[0].type)
        assertEquals(1, feedItems[0].seasonNumber)
    }

    @Test
    fun `should return feed items with review text when rating has review`() {
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com")
        userRepository.save(followedUser)
        userRepository.toggleFollow(User.Id(1), User.Id(2))

        val review = org.ratelog.Review("Great movie!")
        val rating = RatingFactory.aRating(movieId = Movie.Id(1), userId = User.Id(2), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now(), review = review.value)
        ratingRepository.save(rating)

        val query = FeedQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val feedItems = result.getOrElse { emptyList() }
        assertEquals(1, feedItems.size)
        assertEquals("Great movie!", feedItems[0].reviewText)
    }

    @Test
    fun `should return 10 items when limit is 10`() {
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com")
        userRepository.save(followedUser)
        userRepository.toggleFollow(User.Id(1), User.Id(2))

        repeat(15) { i ->
            val rating = RatingFactory.aRating(movieId = Movie.Id(i.toLong()), userId = User.Id(2), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now().minusSeconds(i.toLong() * 60), review = null)
            ratingRepository.save(rating)
        }

        val query = FeedQuery(User.Id(1), limit = 10)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val feedItems = result.getOrElse { emptyList() }
        assertEquals(10, feedItems.size)
    }

    @Test
    fun `should return 20 items when limit is 20`() {
        val followedUser = UserFactory.aUser(id = 2, username = "followeduser", email = "followed@example.com")
        userRepository.save(followedUser)
        userRepository.toggleFollow(User.Id(1), User.Id(2))

        repeat(25) { i ->
            val rating = RatingFactory.aRating(movieId = Movie.Id(i.toLong()), userId = User.Id(2), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now().minusSeconds(i.toLong() * 60), review = null)
            ratingRepository.save(rating)
        }

        val query = FeedQuery(User.Id(1), limit = 20)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val feedItems = result.getOrElse { emptyList() }
        assertEquals(20, feedItems.size)
    }
}
