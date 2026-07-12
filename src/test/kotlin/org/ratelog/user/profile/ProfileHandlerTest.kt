package org.ratelog.user.profile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.Score
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Username
import org.ratelog.feed.FeedItem as FeedRow
import org.ratelog.feed.FeedRepository
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User
import java.time.Instant

class ProfileHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private val feedRepository: FeedRepository = mock()
    private lateinit var handler: ProfileHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = ProfileHandler(userRepository, feedRepository)
    }

    private fun aUser() = UserFactory.aUser(
        id = 1,
        username = "testuser",
        email = "test@example.com",
        lang = Lang.es,
        createdAtEpochMs = 1609459200000
    )

    private fun aFeedRow(
        tmdbId: Int,
        createdAtEpochMs: Long = Instant.now().toEpochMilli(),
    ) = FeedRow(
        tmdbId = TmdbId(tmdbId),
        title = Title("Some title"),
        score = Score(5.0),
        text = null,
        username = Username("testuser"),
        createdAtEpochMs = createdAtEpochMs,
        mediaType = MediaType.movie,
        seasonNumber = null,
    )

    @Test
    fun `should return profile when user exists`() {
        userRepository.save(aUser())

        val query = GetProfile(User.Id(1), User.Id(1), 10, Lang.es)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile ->
                assertEquals("testuser", profile.username.value)
                assertEquals("test@example.com", profile.email.value)
                assertEquals("es", profile.lang.name)
            }
        )
    }

    @Test
    fun `should return UserNotFound when user does not exist`() {
        val query = GetProfile(User.Id(1), User.Id(999), 10, Lang.es)

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertEquals(ProfileHandlerError.UserNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return 10 items and hasMore when limit is 10 and there are more`() {
        userRepository.save(aUser())
        val rows = (1 until 16).map { i -> aFeedRow(tmdbId = i, createdAtEpochMs = Instant.now().minusSeconds(i.toLong() * 60).toEpochMilli()) }
        whenever(feedRepository.findAll(listOf(User.Id(1)), Lang.es, 10)).thenReturn(rows.take(10))
        whenever(feedRepository.count(listOf(User.Id(1)))).thenReturn(15L)

        val query = GetProfile(User.Id(1), User.Id(1), limit = 10, Lang.es)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile ->
                assertEquals(10, profile.feed.size)
                assertTrue(profile.hasMore)
            }
        )
    }

    @Test
    fun `should return 20 items and hasMore when limit is 20 and there are more`() {
        userRepository.save(aUser())
        val rows = (1 until 21).map { i -> aFeedRow(tmdbId = i, createdAtEpochMs = Instant.now().minusSeconds(i.toLong() * 60).toEpochMilli()) }
        whenever(feedRepository.findAll(listOf(User.Id(1)), Lang.es, 20)).thenReturn(rows)
        whenever(feedRepository.count(listOf(User.Id(1)))).thenReturn(25L)

        val query = GetProfile(User.Id(1), User.Id(1), limit = 20, Lang.es)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile ->
                assertEquals(20, profile.feed.size)
                assertTrue(profile.hasMore)
            }
        )
    }

    @Test
    fun `should return all items and hasMore false when limit exceeds total`() {
        userRepository.save(aUser())
        val rows = (1 until 6).map { i -> aFeedRow(tmdbId = i, createdAtEpochMs = Instant.now().minusSeconds(i.toLong() * 60).toEpochMilli()) }
        whenever(feedRepository.findAll(listOf(User.Id(1)), Lang.es, 10)).thenReturn(rows)
        whenever(feedRepository.count(listOf(User.Id(1)))).thenReturn(5L)

        val query = GetProfile(User.Id(1), User.Id(1), limit = 10, Lang.es)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile ->
                assertEquals(5, profile.feed.size)
                assertFalse(profile.hasMore)
            }
        )
    }
}
