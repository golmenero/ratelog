package org.ratelog.user.edit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Username
import org.ratelog.test.FakePasswordEncoder
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class EditUserHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var passwordEncoder: FakePasswordEncoder
    private lateinit var handler: EditUserHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        passwordEncoder = FakePasswordEncoder()
        handler = EditUserHandler(userRepository, passwordEncoder)
    }

    @Test
    fun `should update user successfully when all data is valid`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "olduser",
            email = "old@example.com",
            passwordHash = "encoded_oldpassword",
            lang = Lang("es"),
        )
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("newuser"),
            email = Email("new@example.com"),
            currentPassword = Password("oldpassword"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedUser = userRepository.findById(User.Id(1))
        assertEquals("newuser", updatedUser!!.username.value)
        assertEquals("new@example.com", updatedUser.email.value)
        assertEquals("es", updatedUser.lang.value)
    }

    @Test
    fun `should update password when newPassword is provided`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "encoded_oldpassword",
        )
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("testuser"),
            email = Email("test@example.com"),
            currentPassword = Password("oldpassword"),
            newPassword = Password("newpassword123"),
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedUser = userRepository.findById(User.Id(1))
        assertEquals("encoded_newpassword123", updatedUser!!.passwordHash)
    }

    @Test
    fun `should return UserNotFound when user does not exist`() {
        val command = EditUserCommand(
            userId = User.Id(999),
            username = Username("testuser"),
            email = Email("test@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return InvalidCurrentPassword when current password is wrong`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "encoded_correctpassword",
        )
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("testuser"),
            email = Email("test@example.com"),
            currentPassword = Password("wrongpassword"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.InvalidCurrentPassword, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return EmptyFields when username is blank`() {
        val user = UserFactory.aUser(id = 1)
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username(""),
            email = Email("test@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.EmptyFields, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return EmptyFields when email is blank`() {
        val user = UserFactory.aUser(id = 1)
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("testuser"),
            email = Email(""),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.EmptyFields, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return InvalidUsernameLength when username is too short`() {
        val user = UserFactory.aUser(id = 1)
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("ab"),
            email = Email("test@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.InvalidUsernameLength, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return InvalidPasswordLength when newPassword is too short`() {
        val user = UserFactory.aUser(id = 1)
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("testuser"),
            email = Email("test@example.com"),
            currentPassword = Password("password"),
            newPassword = Password("short"),
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.InvalidPasswordLength, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should return UsernameAlreadyExists when username is taken by another user`() {
        val user1 = UserFactory.aUser(id = 1, username = "user1", email = "user1@example.com")
        val user2 = UserFactory.aUser(id = 2, username = "user2", email = "user2@example.com")
        userRepository.save(user1)
        userRepository.save(user2)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("user2"),
            email = Email("new@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.UsernameAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should not return UsernameAlreadyExists when username is the same as current user`() {
        val user = UserFactory.aUser(id = 1, username = "testuser", email = "test@example.com")
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("testuser"),
            email = Email("new@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
    }

    @Test
    fun `should return EmailAlreadyExists when email is taken by another user`() {
        val user1 = UserFactory.aUser(id = 1, username = "user1", email = "user1@example.com")
        val user2 = UserFactory.aUser(id = 2, username = "user2", email = "user2@example.com")
        userRepository.save(user1)
        userRepository.save(user2)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("newuser"),
            email = Email("user2@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.EmailAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `should not return EmailAlreadyExists when email is the same as current user`() {
        val user = UserFactory.aUser(id = 1, username = "testuser", email = "test@example.com")
        userRepository.save(user)

        val command = EditUserCommand(
            userId = User.Id(1),
            username = Username("newuser"),
            email = Email("test@example.com"),
            currentPassword = Password("password"),
            newPassword = null,
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
    }
}
