package org.raterr.profile

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.user.User
import org.raterr.user.InMemoryUserRepository
import org.raterr.user.profile.GetProfile
import org.raterr.user.profile.ProfileHandler

class ProfileHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val handler = ProfileHandler(userRepository)

    @Test
    fun `returns profile with user data`() {
        userRepository.save(User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hash", createdAtEpochMs = 1700000000000))

        val result = handler.handle(GetProfile(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals("testuser", it.username)
                assertEquals("test@example.com", it.email)
            }
        )
    }

    @Test
    fun `UserNotFound returns Left when user does not exist`() {
        val result = handler.handle(GetProfile(UserId(999)))

        assertTrue(result.isLeft())
    }
}
