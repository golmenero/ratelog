package org.ratelog.feed.community

import arrow.core.getOrElse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.Review
import org.ratelog.ReviewText
import org.ratelog.ReviewTitle
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Username
import org.ratelog.feed.FeedRepository
import org.ratelog.test.UserFactory
import org.ratelog.feed.FeedItem as FeedRow
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import java.time.Instant

class CommunityHandlerTest {

    private var feedRepository: FeedRepository = mock()
    private var userRepository: UserRepository = mock()
    private lateinit var handler: CommunityHandler

    private val userId = User.Id(1)
    private val followedId = User.Id(2)
    private val followedUser = UserFactory.aUser(id = 2, username = "followeduser")
    private val userIds = listOf(followedId, userId)

    @BeforeEach
    fun setUp() {
        handler = CommunityHandler(feedRepository, userRepository)
    }

    private fun stubFollowing(users: List<User>) {
        whenever(userRepository.findFollowingByUserId(userId)).thenReturn(users)
    }

    private fun aFeedRow(
        tmdbId: Int = 1,
        title: String = "Some title",
        score: Double? = 3.0,
        reviewTitle: String? = null,
        reviewText: String? = null,
        username: String = "followeduser",
        createdAtEpochMs: Long = Instant.now().toEpochMilli(),
        mediaType: MediaType = MediaType.movie,
        seasonNumber: Int? = null,
    ) = FeedRow(
        tmdbId = TmdbId(tmdbId),
        title = Title(title),
        score = score?.let(::Score),
        review = toReview(reviewTitle, reviewText),
        username = Username(username),
        createdAtEpochMs = createdAtEpochMs,
        mediaType = mediaType,
        seasonNumber = seasonNumber?.let(::SeasonNumber),
    )

    private fun toReview(reviewTitle: String?, reviewText: String?): Review? {
        val title = reviewTitle?.takeIf { it.isNotBlank() }?.let(::ReviewTitle)
        val text = reviewText?.takeIf { it.isNotBlank() }?.let(::ReviewText)
        return if (title == null && text == null) null else Review(title, text)
    }

    @Test
    fun `should include own posts when user follows no one`() {
        stubFollowing(emptyList())
        val row = aFeedRow(username = "currentuser")
        whenever(feedRepository.findAll(listOf(userId), Lang.en, 10)).thenReturn(listOf(row))
        whenever(feedRepository.count(listOf(userId))).thenReturn(1L)

        val result = handler.handle(FeedQuery(userId, 10, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(1, feedResult.feed.size)
        assertFalse(feedResult.hasMore)
    }

    @Test
    fun `should return feed items from followed users and own ratings`() {
        stubFollowing(listOf(followedUser))
        val row = aFeedRow(mediaType = MediaType.movie, seasonNumber = null)
        whenever(feedRepository.findAll(userIds, Lang.en, 10)).thenReturn(listOf(row))
        whenever(feedRepository.count(userIds)).thenReturn(1L)

        val result = handler.handle(FeedQuery(userId, 10, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(1, feedResult.feed.size)
        assertEquals(Username("followeduser"), feedResult.feed[0].username)
        assertEquals(MediaType.movie, feedResult.feed[0].mediaType)
        assertNull(feedResult.feed[0].review)
        assertNull(feedResult.feed[0].seasonNumber)
        assertFalse(feedResult.hasMore)
    }

    @Test
    fun `should return feed items from followed users tv ratings`() {
        stubFollowing(listOf(followedUser))
        val row = aFeedRow(mediaType = MediaType.tvshow, seasonNumber = 1)
        whenever(feedRepository.findAll(userIds, Lang.en, 10)).thenReturn(listOf(row))
        whenever(feedRepository.count(userIds)).thenReturn(1L)

        val result = handler.handle(FeedQuery(userId, 10, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(1, feedResult.feed.size)
        assertEquals(Username("followeduser"), feedResult.feed[0].username)
        assertEquals(MediaType.tvshow, feedResult.feed[0].mediaType)
        assertEquals(SeasonNumber(1), feedResult.feed[0].seasonNumber)
        assertFalse(feedResult.hasMore)
    }

    @Test
    fun `should return feed items with review text when rating has review`() {
        stubFollowing(listOf(followedUser))
        val row = aFeedRow(reviewTitle = "Great movie", reviewText = "Great movie!")
        whenever(feedRepository.findAll(userIds, Lang.en, 10)).thenReturn(listOf(row))
        whenever(feedRepository.count(userIds)).thenReturn(1L)

        val result = handler.handle(FeedQuery(userId, 10, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(1, feedResult.feed.size)
        assertEquals("Great movie", feedResult.feed[0].review?.title?.value)
        assertEquals("Great movie!", feedResult.feed[0].review?.text?.value)
        assertFalse(feedResult.hasMore)
    }

    @Test
    fun `should return 10 items and hasMore when there are more than 10 items`() {
        stubFollowing(listOf(followedUser))
        val rows = (1 until 11).map { i -> aFeedRow(tmdbId = i, createdAtEpochMs = Instant.now().minusSeconds(i.toLong() * 60).toEpochMilli()) }
        whenever(feedRepository.findAll(userIds, Lang.en, 10)).thenReturn(rows)
        whenever(feedRepository.count(userIds)).thenReturn(15L)

        val result = handler.handle(FeedQuery(userId, limit = 10, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(10, feedResult.feed.size)
        assertTrue(feedResult.hasMore)
    }

    @Test
    fun `should return 20 items and hasMore when there are more than 20 items`() {
        stubFollowing(listOf(followedUser))
        val rows = (1 until 21).map { i -> aFeedRow(tmdbId = i, createdAtEpochMs = Instant.now().minusSeconds(i.toLong() * 60).toEpochMilli()) }
        whenever(feedRepository.findAll(userIds, Lang.en, 20)).thenReturn(rows)
        whenever(feedRepository.count(userIds)).thenReturn(25L)

        val result = handler.handle(FeedQuery(userId, limit = 20, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(20, feedResult.feed.size)
        assertTrue(feedResult.hasMore)
    }

    @Test
    fun `should return all items and hasMore false when limit exceeds total`() {
        stubFollowing(listOf(followedUser))
        val rows = (1 until 6).map { i -> aFeedRow(tmdbId = i, createdAtEpochMs = Instant.now().minusSeconds(i.toLong() * 60).toEpochMilli()) }
        whenever(feedRepository.findAll(userIds, Lang.en, 10)).thenReturn(rows)
        whenever(feedRepository.count(userIds)).thenReturn(5L)

        val result = handler.handle(FeedQuery(userId, limit = 10, Lang.en))

        assertTrue(result.isRight())
        val feedResult = result.getOrElse { fail("expected Right") }
        assertEquals(5, feedResult.feed.size)
        assertFalse(feedResult.hasMore)
    }
}
