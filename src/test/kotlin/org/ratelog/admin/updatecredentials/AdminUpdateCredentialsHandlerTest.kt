package org.ratelog.admin.updatecredentials

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Email
import org.ratelog.Password
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.test.FakePasswordEncoder
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class AdminUpdateCredentialsHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var passwordEncoder: FakePasswordEncoder
    private lateinit var handler: AdminUpdateCredentialsHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        passwordEncoder = FakePasswordEncoder()
        handler = AdminUpdateCredentialsHandler(userRepository, passwordEncoder)
    }

    @Test
    fun `given a USER current user when updating credentials then returns Forbidden`() {
        // Given
        val current = userRepository.save(UserFactory.aUser(username = "normal"))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = current,
                targetUserId = target.id!!,
                newUsername = Username("newName"),
                newEmail = target.email,
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateCredentialsHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating username then target is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "oldName"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = target.id!!,
                newUsername = Username("newName"),
                newEmail = target.email,
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Username("newName"), updated!!.username)
        val persisted = userRepository.findById(target.id)
        assertEquals(target.passwordHash, persisted!!.passwordHash)
    }

    @Test
    fun `given admin updating email then target email is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "old@example.com"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = target.id!!,
                newUsername = target.username,
                newEmail = Email("new@example.com"),
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Email("new@example.com"), updated!!.email)
        val persisted = userRepository.findById(target.id)
        assertEquals(Email("new@example.com"), persisted!!.email)
    }

    @Test
    fun `given admin updating superadmin email then succeeds`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN, email = "old@root.com"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = target.id!!,
                newUsername = target.username,
                newEmail = Email("new@root.com"),
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Email("new@root.com"), updated!!.email)
    }

    @Test
    fun `given admin updating password then target password hash changes`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = target.id!!,
                newUsername = target.username,
                newEmail = target.email,
                newPassword = Password("NewPass1!"),
            )
        )

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals("encoded_NewPass1!", updated!!.passwordHash)
    }

    @Test
    fun `given admin updating to a username taken by another user then returns UsernameAlreadyExists`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))
        userRepository.save(UserFactory.aUser(username = "taken"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = target.id!!,
                newUsername = Username("taken"),
                newEmail = target.email,
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateCredentialsHandlerError.UsernameAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating to an email taken by another user then returns EmailAlreadyExists`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "victim@example.com"))
        userRepository.save(UserFactory.aUser(username = "taken", email = "taken@example.com"))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = target.id!!,
                newUsername = target.username,
                newEmail = Email("taken@example.com"),
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateCredentialsHandlerError.EmailAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating own email to the same value then succeeds`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = admin.id!!,
                newUsername = admin.username,
                newEmail = admin.email,
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `given admin updating own username to the same value then succeeds`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = admin.id!!,
                newUsername = admin.username,
                newEmail = admin.email,
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `given admin updating a non-existent user then returns UserNotFound`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminUpdateCredentialsCommand(
                currentUser = admin,
                targetUserId = User.Id(9999),
                newUsername = Username("whatever"),
                newEmail = Email("whatever@example.com"),
                newPassword = null,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateCredentialsHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }
}
