package org.raterr.profile

import arrow.core.right
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.follow.InMemoryUserFollowRepository
import org.raterr.premieres.ListPremiereHandler
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbMovie
import org.raterr.user.User
import org.raterr.user.UserRepository
import java.util.Optional

class ProfileHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val followRepository = InMemoryFollowRepository()
    private val userRepository: UserRepository = mock()
    private val userFollowRepository = InMemoryUserFollowRepository()
    private val premiereHandler = ListPremiereHandler(tmdbClient, followRepository)
    private val handler = ProfileHandler(userRepository, premiereHandler, userFollowRepository)

    @BeforeEach
    fun setUp() {
        followRepository.clear()
        userFollowRepository.clear()
    }

    @Test
    fun `returns profile with user data and empty premieres when no follows`() {
        val user = User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user))

        val result = handler.handle(GetProfile(UserId(1)))

        assertTrue(result.isRight())
        val profile = (result as arrow.core.Either.Right).value
        assertEquals("testuser", profile.username)
        assertEquals("test@example.com", profile.email)
        assertEquals(0, profile.premieres.released.size)
        assertEquals(0, profile.premieres.upcoming.size)
        assertEquals(0, profile.premieres.noDate.size)
        assertEquals(0, profile.following.size)
    }

    @Test
    fun `returns profile with premieres when user has follows`() {
        val user = User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user))
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 1))
        whenever(tmdbClient.movieDetails(1)).thenReturn(
            TmdbMovie(id = 1, title = "Movie1", releaseDate = "2024-01-01").right()
        )

        val result = handler.handle(GetProfile(UserId(1)))

        assertTrue(result.isRight())
        val profile = (result as arrow.core.Either.Right).value
        assertEquals("testuser", profile.username)
        assertEquals(1, profile.premieres.released.size)
        assertEquals("Movie1", profile.premieres.released[0].title)
    }

    @Test
    fun `returns profile with following when user follows other users`() {
        val user = User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user))
        userFollowRepository.addUser(2, "user2")
        userFollowRepository.addUser(3, "user3")
        userFollowRepository.save(org.raterr.follow.UserFollow(followerId = 1, followedId = 2))
        userFollowRepository.save(org.raterr.follow.UserFollow(followerId = 1, followedId = 3))

        val result = handler.handle(GetProfile(UserId(1)))

        assertTrue(result.isRight())
        val profile = (result as arrow.core.Either.Right).value
        assertEquals(2, profile.following.size)
    }

    @Test
    fun `UserNotFound returns Left when user does not exist`() {
        whenever(userRepository.findById(999)).thenReturn(Optional.empty())

        val result = handler.handle(GetProfile(UserId(999)))

        assertTrue(result.isLeft())
    }
}
