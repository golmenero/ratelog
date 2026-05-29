package org.ratelog.user.profile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class ProfileHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: ProfileHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = ProfileHandler(userRepository)
    }

    @Test
    fun `should return profile when user exists`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            createdAtEpochMs = 1609459200000
        )
        userRepository.save(user)

        val query = GetProfile(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile ->
                assertEquals("testuser", profile.username.value)
                assertEquals("test@example.com", profile.email.value)
            }
        )
    }

    @Test
    fun `should return UserNotFound when user does not exist`() {
        val query = GetProfile(User.Id(999))

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertEquals(ProfileHandlerError.UserNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
