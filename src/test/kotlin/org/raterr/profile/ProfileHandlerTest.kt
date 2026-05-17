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
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbMovie
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.raterr.user.profile.GetProfile
import org.raterr.user.profile.ProfileHandler
import java.util.Optional

class ProfileHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val followRepository = InMemoryFollowRepository()
    private val userRepository: UserRepository = mock()
    private val handler = ProfileHandler(userRepository)

    @BeforeEach
    fun setUp() {
        followRepository.clear()
    }

    @Test
    fun `returns profile with user data`() {
        val user = User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findById(1)).thenReturn(Optional.of(user))

        val result = handler.handle(GetProfile(UserId(1)))

        assertTrue(result.isRight())
        val profile = (result as arrow.core.Either.Right).value
        assertEquals("testuser", profile.username)
        assertEquals("test@example.com", profile.email)
    }

    @Test
    fun `UserNotFound returns Left when user does not exist`() {
        whenever(userRepository.findById(999)).thenReturn(Optional.empty())

        val result = handler.handle(GetProfile(UserId(999)))

        assertTrue(result.isLeft())
    }
}
