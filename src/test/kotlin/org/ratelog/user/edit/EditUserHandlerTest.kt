package org.ratelog.user.edit

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

    private fun command(
        current: User,
        target: User,
        username: Username = target.username,
        email: Email = target.email,
        currentPassword: Password? = null,
        newPassword: Password? = null,
        role: Role? = null,
    ) = EditUserCommand(
        currentUser = current,
        targetUserId = target.id!!,
        newUsername = username,
        newEmail = email,
        currentPassword = currentPassword,
        newPassword = newPassword,
        newRole = role,
    )

    @Test
    fun `given a user editing themselves with the right current password then data is updated`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal", email = "old@example.com"))

        // When
        val result = handler.handle(
            command(user, user, username = Username("renamed"), email = Email("new@example.com"), currentPassword = Password("password"))
        )

        // Then
        assertTrue(result.isRight())
        val persisted = userRepository.findById(user.id!!)!!
        assertEquals(Username("renamed"), persisted.username)
        assertEquals(Email("new@example.com"), persisted.email)
    }

    @Test
    fun `given a user editing themselves with the right current password then the new password is applied`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))

        // When
        val result = handler.handle(
            command(user, user, currentPassword = Password("password"), newPassword = Password("NewPass1!"))
        )

        // Then
        assertTrue(result.isRight())
        assertEquals("encoded_NewPass1!", userRepository.findById(user.id!!)!!.passwordHash)
    }

    @Test
    fun `given a user editing themselves with a wrong current password then returns InvalidCurrentPassword`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))

        // When
        val result = handler.handle(command(user, user, currentPassword = Password("wrongpassword")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.InvalidCurrentPassword, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a user editing themselves without current password then returns MissingCurrentPassword`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))

        // When
        val result = handler.handle(command(user, user, newPassword = Password("NewPass1!")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.MissingCurrentPassword, result.fold({ it }, { Unit }))
        assertEquals("encoded_password", userRepository.findById(user.id!!)!!.passwordHash)
    }

    @Test
    fun `given a user editing another user then returns Forbidden`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))
        val victim = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(user, victim, currentPassword = Password("password")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a user editing themselves with a role change then returns CannotChangeRole`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))

        // When
        val result = handler.handle(
            command(user, user, currentPassword = Password("password"), role = Role.ADMIN)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.CannotChangeRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an admin editing a user with the right current password then data is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "old@example.com"))

        // When
        val result = handler.handle(
            command(admin, target, email = Email("new@example.com"), currentPassword = Password("password"))
        )

        // Then
        assertTrue(result.isRight())
        assertEquals(Email("new@example.com"), userRepository.findById(target.id!!)!!.email)
    }

    @Test
    fun `given an admin editing a user without current password then returns MissingCurrentPassword`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(admin, target, newPassword = Password("NewPass1!")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.MissingCurrentPassword, result.fold({ it }, { Unit }))
        assertEquals("encoded_password", userRepository.findById(target.id!!)!!.passwordHash)
    }

    @Test
    fun `given an admin editing another admin then returns CannotEditAdmin`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val otherAdmin = userRepository.save(UserFactory.aUser(username = "admin2", role = Role.ADMIN))

        // When
        val result = handler.handle(command(admin, otherAdmin, currentPassword = Password("password")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.CannotEditAdmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an admin editing themselves then data is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", email = "old@example.com", role = Role.ADMIN))

        // When
        val result = handler.handle(
            command(admin, admin, email = Email("new@example.com"), currentPassword = Password("password"))
        )

        // Then
        assertTrue(result.isRight())
        assertEquals(Email("new@example.com"), userRepository.findById(admin.id!!)!!.email)
    }

    @Test
    fun `given a superadmin editing a user without current password then data is updated and role is preserved`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "old@example.com"))

        // When
        val result = handler.handle(
            command(superadmin, target, email = Email("new@example.com"), newPassword = Password("NewPass1!"))
        )

        // Then
        assertTrue(result.isRight())
        val persisted = userRepository.findById(target.id!!)!!
        assertEquals(Email("new@example.com"), persisted.email)
        assertEquals("encoded_NewPass1!", persisted.passwordHash)
        assertEquals(Role.USER, persisted.role)
    }

    @Test
    fun `given a superadmin editing themselves without current password then data is updated and role is preserved`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", email = "old@example.com", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(
            command(superadmin, superadmin, email = Email("new@example.com"), newPassword = Password("NewPass1!"))
        )

        // Then
        assertTrue(result.isRight())
        val persisted = userRepository.findById(superadmin.id!!)!!
        assertEquals(Email("new@example.com"), persisted.email)
        assertEquals("encoded_NewPass1!", persisted.passwordHash)
        assertEquals(Role.SUPERADMIN, persisted.role)
    }

    @Test
    fun `given a superadmin editing a user with a wrong current password then returns InvalidCurrentPassword`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(superadmin, target, currentPassword = Password("wrongpassword")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.InvalidCurrentPassword, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a superadmin promoting a user to admin then target role becomes admin`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(superadmin, target, role = Role.ADMIN))

        // Then
        assertTrue(result.isRight())
        assertEquals(Role.ADMIN, userRepository.findById(target.id!!)!!.role)
    }

    @Test
    fun `given a superadmin demoting an admin then target role becomes user`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", role = Role.ADMIN))

        // When
        val result = handler.handle(command(superadmin, target, role = Role.USER))

        // Then
        assertTrue(result.isRight())
        assertEquals(Role.USER, userRepository.findById(target.id!!)!!.role)
    }

    @Test
    fun `given a superadmin promoting to superadmin then returns CannotPromoteToSuperadmin`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(superadmin, target, role = Role.SUPERADMIN))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.CannotPromoteToSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a superadmin changing another superadmin role then returns CannotChangeSuperadminRole`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val otherSuperadmin = userRepository.save(UserFactory.aUser(username = "root2", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(command(superadmin, otherSuperadmin, role = Role.USER))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.CannotChangeSuperadminRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a superadmin demoting themselves then returns CannotChangeSuperadminRole`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(command(superadmin, superadmin, role = Role.ADMIN))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.CannotChangeSuperadminRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a username taken by another user then returns UsernameAlreadyExists`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "user1"))
        userRepository.save(UserFactory.aUser(username = "taken"))

        // When
        val result = handler.handle(
            command(user, user, username = Username("taken"), currentPassword = Password("password"))
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.UsernameAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an email taken by another user then returns EmailAlreadyExists`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "user1"))
        userRepository.save(UserFactory.aUser(username = "user2", email = "taken@example.com"))

        // When
        val result = handler.handle(
            command(user, user, email = Email("taken@example.com"), currentPassword = Password("password"))
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.EmailAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a non existing user then returns UserNotFound`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val ghost = User(
            id = User.Id(9999),
            username = Username("ghost"),
            email = Email("ghost@example.com"),
            passwordHash = "encoded_password",
            createdAtEpochMs = 0,
            lang = org.ratelog.Lang.en,
            metadataLang = org.ratelog.Lang.en,
            role = Role.USER,
        )

        // When
        val result = handler.handle(command(admin, ghost, currentPassword = Password("password")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(EditUserHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }
}
