package org.raterr.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.user.profile.GetProfile
import org.raterr.user.profile.ProfileHandler

class ProfileHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val handler = ProfileHandler(userRepository)

    @Test
    fun `returns profile with user data`() {
        userRepository.save(User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hash", createdAtEpochMs = 1700000000000))

        val result = handler.handle(GetProfile(UserId(1)))

        Assertions.assertTrue(result.isRight())
        result.fold(
            { },
            {
                Assertions.assertEquals("testuser", it.username)
                Assertions.assertEquals("test@example.com", it.email)
            }
        )
    }

    @Test
    fun `UserNotFound returns Left when user does not exist`() {
        val result = handler.handle(GetProfile(UserId(999)))

        Assertions.assertTrue(result.isLeft())
    }
}