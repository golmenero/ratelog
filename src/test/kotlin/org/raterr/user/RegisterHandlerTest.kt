package org.raterr.user

import arrow.core.Either
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.raterr.rating.Rating
import org.raterr.rating.InMemoryRatingRepository
import org.raterr.user.InMemoryUserRepository
import org.raterr.user.register.RegisterHandler
import org.raterr.user.register.RegisterHandlerError
import org.raterr.user.register.RegisterUser
import org.springframework.security.crypto.password.PasswordEncoder

class RegisterHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val passwordEncoder: PasswordEncoder = mock()
    private val ratingRepository = InMemoryRatingRepository()
    private val handler = RegisterHandler(userRepository, passwordEncoder, ratingRepository)

    @BeforeEach
    fun setUp() {
        userRepository.clear()
        ratingRepository.clear()
    }

    @Test
    fun `happy path returns Right`() {
        whenever(passwordEncoder.encode("password123")).thenReturn("hashed")

        val result = handler.handle(RegisterUser("testuser", "test@example.com", "password123"))

        Assertions.assertTrue(result.isRight())
    }

    @Test
    fun `blank username returns EmptyFields`() {
        val result = handler.handle(RegisterUser("", "test@example.com", "password123"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.EmptyFields)
    }

    @Test
    fun `blank email returns EmptyFields`() {
        val result = handler.handle(RegisterUser("testuser", "", "password123"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.EmptyFields)
    }

    @Test
    fun `blank password returns EmptyFields`() {
        val result = handler.handle(RegisterUser("testuser", "test@example.com", ""))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.EmptyFields)
    }

    @Test
    fun `username too short returns InvalidUsernameLength`() {
        val result = handler.handle(RegisterUser("ab", "test@example.com", "password123"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.InvalidUsernameLength)
    }

    @Test
    fun `username too long returns InvalidUsernameLength`() {
        val result = handler.handle(RegisterUser("a".repeat(51), "test@example.com", "password123"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.InvalidUsernameLength)
    }

    @Test
    fun `password too short returns InvalidPasswordLength`() {
        val result = handler.handle(RegisterUser("testuser", "test@example.com", "1234567"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.InvalidPasswordLength)
    }

    @Test
    fun `duplicate username returns UsernameAlreadyExists`() {
        userRepository.save(User(username = "testuser", email = "test@example.com", passwordHash = "hashed"))

        val result = handler.handle(RegisterUser("testuser", "other@example.com", "password123"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.UsernameAlreadyExists)
    }

    @Test
    fun `duplicate email returns EmailAlreadyExists`() {
        userRepository.save(User(username = "otheruser", email = "test@example.com", passwordHash = "hashed"))

        val result = handler.handle(RegisterUser("testuser", "test@example.com", "password123"))

        Assertions.assertTrue(result.isLeft())
        Assertions.assertTrue((result as Either.Left).value is RegisterHandlerError.EmailAlreadyExists)
    }

    @Test
    fun `migrates orphan ratings to new user`() {
        whenever(passwordEncoder.encode("password123")).thenReturn("hashed")
        val orphanRating = ratingRepository.save(
            Rating(
                id = 1,
                movieId = 10,
                userId = 0,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        handler.handle(RegisterUser("testuser", "test@example.com", "password123"))

        val savedUser = userRepository.findByUsername("testuser").get()
        val migratedRatings = ratingRepository.findByUserId(savedUser.id!!)
        assertEquals(1, migratedRatings.size)
        assertEquals(savedUser.id, migratedRatings[0].userId)
    }
}
