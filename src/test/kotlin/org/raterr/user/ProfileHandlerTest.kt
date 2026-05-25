package org.raterr.user

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.raterr.Email
import org.raterr.Username
import org.raterr.user.User.Id
import org.raterr.user.profile.GetProfile
import org.raterr.user.profile.ProfileHandler

class ProfileHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val handler = ProfileHandler(userRepository)

    @Test
    fun `returns profile with user data`() {
        userRepository.save(User(id = Id(1), username = Username("testuser"), email = Email("test@example.com"), passwordHash = "hash", createdAtEpochMs = 1700000000000))

        val result = handler.handle(GetProfile(User.Id(1)))

        Assertions.assertTrue(result.isRight())
        result.fold(
            { },
            {
                Assertions.assertEquals("testuser", it.username.value)
                Assertions.assertEquals("test@example.com", it.email.value)
            }
        )
    }

    @Test
    fun `UserNotFound returns Left when user does not exist`() {
        val result = handler.handle(GetProfile(User.Id(999)))

        Assertions.assertTrue(result.isLeft())
    }
}