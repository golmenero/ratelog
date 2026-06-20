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
        ratingRepository = InMemoryRatingRepository(userRepository)
        tvRatingRepository = InMemoryTvRatingRepository(userRepository)
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

    @Test
    fun `should return first page of ratings when page is 0`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            lang = Lang.es,
            createdAtEpochMs = 1609459200000
        )
        userRepository.save(user)

        repeat(15) { i ->
            val rating = RatingFactory.aRating(movieId = org.ratelog.movie.Movie.Id(i.toLong()), userId = User.Id(1), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now().minusSeconds(i.toLong() * 60), review = null)
            ratingRepository.save(rating)
        }

        val query = GetProfile(User.Id(1), User.Id(1), page = 0)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile -> assertEquals(10, profile.ratings.size) }
        )
    }

    @Test
    fun `should return second page of ratings when page is 1`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            lang = Lang.es,
            createdAtEpochMs = 1609459200000
        )
        userRepository.save(user)

        repeat(15) { i ->
            val rating = RatingFactory.aRating(movieId = org.ratelog.movie.Movie.Id(i.toLong()), userId = User.Id(1), directing = 5.0, cinematography = 5.0, acting = 5.0, soundtrack = 5.0, screenplay = 5.0, createdAt = Instant.now().minusSeconds(i.toLong() * 60), review = null)
            ratingRepository.save(rating)
        }

        val query = GetProfile(User.Id(1), User.Id(1), page = 1)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { profile -> assertEquals(5, profile.ratings.size) }
        )
    }
}
