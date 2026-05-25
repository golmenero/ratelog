package org.raterr.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.Email
import org.raterr.Password
import org.raterr.Username
import org.raterr.rating.InMemoryRatingRepository
import org.raterr.rating.aRating
import org.raterr.user.register.RegisterHandler
import org.raterr.user.register.RegisterHandlerError
import org.raterr.user.register.RegisterUser
import org.springframework.security.crypto.password.PasswordEncoder

class RegisterHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val passwordEncoder = FakePasswordEncoder()
    private val ratingRepository = InMemoryRatingRepository()
    private val handler = RegisterHandler(userRepository, passwordEncoder, ratingRepository)

    @BeforeEach
    fun setUp() {
        userRepository.clear()
        ratingRepository.clear()
        passwordEncoder.clear()
    }

    @Test
    fun `happy path returns Right`() {
        val result = handler.handle(RegisterUser(Username("testuser"), Email("test@example.com"), Password("password123")))

        assertTrue(result.isRight())
    }

    @Test
    fun `blank username returns EmptyFields`() {
        val result = handler.handle(RegisterUser(Username(""), Email("test@example.com"), Password("password123")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.EmptyFields) },
            { }
        )
    }

    @Test
    fun `blank email returns EmptyFields`() {
        val result = handler.handle(RegisterUser(
            Username("testuser"), Email(""), Password("password123")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.EmptyFields) },
            { }
        )
    }

    @Test
    fun `blank password returns EmptyFields`() {
        val result = handler.handle(RegisterUser(Username("testuser"), Email("test@example.com"), Password("")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.EmptyFields) },
            { }
        )
    }

    @Test
    fun `username too short returns InvalidUsernameLength`() {
        val result = handler.handle(RegisterUser(Username("ab"), Email("test@example.com"), Password("password123")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.InvalidUsernameLength) },
            { }
        )
    }

    @Test
    fun `username too long returns InvalidUsernameLength`() {
        val result = handler.handle(RegisterUser(Username("a".repeat(51)), Email("test@example.com"), Password("password123")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.InvalidUsernameLength) },
            { }
        )
    }

    @Test
    fun `password too short returns InvalidPasswordLength`() {
        val result = handler.handle(RegisterUser(Username("testuser"), Email("test@example.com"), Password("1234567")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.InvalidPasswordLength) },
            { }
        )
    }

    @Test
    fun `duplicate username returns UsernameAlreadyExists`() {
        userRepository.save(User(id = null, username = Username("testuser"), email = Email("test@example.com"), passwordHash = "hashed"))

        val result = handler.handle(RegisterUser(Username("testuser"), Email("other@example.com"), Password("password123")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.UsernameAlreadyExists) },
            { }
        )
    }

    @Test
    fun `duplicate email returns EmailAlreadyExists`() {
        userRepository.save(User(id = null, username =Username( "otheruser"), email = Email("test@example.com"), passwordHash = "hashed"))

        val result = handler.handle(RegisterUser(Username("testuser"), Email("test@example.com"), Password("password123")))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is RegisterHandlerError.EmailAlreadyExists) },
            { }
        )
    }

    @Test
    fun `migrates orphan ratings to new user`() {
        ratingRepository.save(
            aRating(
                id = org.raterr.rating.Rating.Id(1),
                movieId = org.raterr.movie.Movie.Id(10),
                userId = User.Id(0)
            )
        )

        handler.handle(RegisterUser(Username("testuser"), Email("test@example.com"), Password("password123")))

        val savedUser = userRepository.findByUsername(Username("testuser"))
        val migratedRatings = ratingRepository.findByUserId(savedUser!!.id!!)
        assertEquals(1, migratedRatings.size)
        assertEquals(savedUser.id.value, migratedRatings[0].userId.value)
    }
}

class FakePasswordEncoder : PasswordEncoder {
    private val store = mutableMapOf<String, String>()

    fun clear() {
        store.clear()
    }

    override fun encode(rawPassword: CharSequence): String =
        store.getOrPut(rawPassword.toString()) { "hashed_${rawPassword}" }

    override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean =
        encode(rawPassword) == encodedPassword
}
