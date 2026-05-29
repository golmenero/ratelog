package org.ratelog.user.register

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.test.FakePasswordEncoder
import org.ratelog.test.InMemoryUserRepository

class RegisterHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var passwordEncoder: FakePasswordEncoder
    private lateinit var handler: RegisterHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        passwordEncoder = FakePasswordEncoder()
        handler = RegisterHandler(userRepository, passwordEncoder)
    }

    @Test
    fun `should register user successfully when all data is valid`() {
        val command = RegisterUser(
            username = Username("testuser"),
            email = Email("test@example.com"),
            Password("password123")
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val savedUser = userRepository.findByUsername(Username("testuser"))
        assertNotNull(savedUser)
        assertEquals("encoded_password123", savedUser!!.passwordHash)
    }

    @Test
    fun `should return EmptyFields error when username is blank`() {
        val command = RegisterUser(
            username = Username(""),
            email = Email("test@example.com"),
            Password("password123")
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(RegisterHandlerError.EmptyFields, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return EmptyFields error when email is blank`() {
        val command = RegisterUser(
            username = Username("testuser"),
            Email(""),
            Password("password123")
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return EmptyFields error when password is blank`() {
        val command = RegisterUser(
            username = Username("testuser"),
            email = Email("test@example.com"),
            Password("")
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return InvalidUsernameLength when username is too short`() {
        val command = RegisterUser(
            username = Username("ab"),
            email = Email("test@example.com"),
            Password("password123")
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(RegisterHandlerError.InvalidUsernameLength, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return InvalidUsernameLength when username is too long`() {
        val command = RegisterUser(
            username = Username("a".repeat(51)),
            email = Email("test@example.com"),
            Password("password123")
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
    }

    @Test
    fun `should return InvalidPasswordLength when password is too short`() {
        val command = RegisterUser(
            username = Username("testuser"),
            email = Email("test@example.com"),
            Password("short")
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(RegisterHandlerError.InvalidPasswordLength, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return UsernameAlreadyExists when username exists`() {
        val existingCommand = RegisterUser(
            username = Username("testuser"),
            email = Email("existing@example.com"),
            Password("password123")
        )
        handler.handle(existingCommand)

        val newCommand = RegisterUser(
            username = Username("testuser"),
            email = Email("new@example.com"),
            Password("password123")
        )

        val result = handler.handle(newCommand)

        assertTrue(result.isLeft())
        assertEquals(RegisterHandlerError.UsernameAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return EmailAlreadyExists when email exists`() {
        val existingCommand = RegisterUser(
            username = Username("existinguser"),
            email = Email("test@example.com"),
            Password("password123")
        )
        handler.handle(existingCommand)

        val newCommand = RegisterUser(
            username = Username("newuser"),
            Email("test@example.com"),
            Password("password123")
        )

        val result = handler.handle(newCommand)

        assertTrue(result.isLeft())
        assertEquals(RegisterHandlerError.EmailAlreadyExists, result.fold({ it }, { Unit }))
    }
}
