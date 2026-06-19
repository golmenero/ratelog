package org.ratelog.user.profile

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Lang
import org.ratelog.test.InMemoryRatingRepository
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.RatingFactory
import org.ratelog.test.TvRatingFactory
import org.ratelog.test.UserFactory
import org.ratelog.user.User
import java.time.Instant

class ProfileHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var ratingRepository: InMemoryRatingRepository
    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var handler: ProfileHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        ratingRepository = InMemoryRatingRepository()
        tvRatingRepository = InMemoryTvRatingRepository()
        handler = ProfileHandler(userRepository, ratingRepository, tvRatingRepository)
    }

    @Test
    fun `should return profile when user exists`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            lang = Lang.es,
            createdAtEpochMs = 1609459200000
        )
        userRepository.save(user)

        val query = GetProfile(User.Id(1), User.Id(1))
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
        val query = GetProfile(User.Id(1), User.Id(999))

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertEquals(ProfileHandlerError.UserNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
