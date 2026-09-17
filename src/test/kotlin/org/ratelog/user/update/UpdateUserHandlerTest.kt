package org.ratelog.user.update

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

class UpdateUserHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var passwordEncoder: FakePasswordEncoder
    private lateinit var handler: UpdateUserHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        passwordEncoder = FakePasswordEncoder()
        handler = UpdateUserHandler(userRepository, passwordEncoder)
    }

    private fun command(
        current: User,
        target: User,
        username: Username = target.username,
        email: Email = target.email,
        password: Password? = null,
        role: Role = target.role,
    ) = UpdateUserCommand(
        currentUser = current,
        targetUserId = target.id!!,
        newUsername = username,
        newEmail = email,
        newPassword = password,
        newRole = role,
    )

    @Test
    fun `given a USER current user when updating then returns Forbidden`() {
        // Given
        val current = userRepository.save(UserFactory.aUser(username = "normal"))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(current, target))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating username then target username is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", email = "admin@example.com", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "oldName", email = "old@example.com"))

        // When
        val result = handler.handle(command(admin, target, username = Username("newName")))

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Username("newName"), updated!!.username)
        val persisted = userRepository.findById(target.id!!)
        assertEquals(target.passwordHash, persisted!!.passwordHash)
    }

    @Test
    fun `given admin updating email then target email is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "old@example.com"))

        // When
        val result = handler.handle(command(admin, target, email = Email("new@example.com")))

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Email("new@example.com"), updated!!.email)
    }

    @Test
    fun `given admin updating superadmin email then succeeds`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN, email = "old@root.com"))

        // When
        val result = handler.handle(command(admin, target, email = Email("new@root.com")))

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Email("new@root.com"), updated!!.email)
    }

    @Test
    fun `given admin updating password then target password hash changes`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", email = "admin@example.com", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "victim@example.com"))

        // When
        val result = handler.handle(command(admin, target, password = Password("NewPass1!")))

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals("encoded_NewPass1!", updated!!.passwordHash)
    }

    @Test
    fun `given admin updating to a username taken by another user then returns UsernameAlreadyExists`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", email = "admin@example.com", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "victim@example.com"))
        userRepository.save(UserFactory.aUser(username = "taken", email = "taken1@example.com"))

        // When
        val result = handler.handle(command(admin, target, username = Username("taken")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.UsernameAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating to an email taken by another user then returns EmailAlreadyExists`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "victim@example.com"))
        userRepository.save(UserFactory.aUser(username = "taken", email = "taken@example.com"))

        // When
        val result = handler.handle(command(admin, target, email = Email("taken@example.com")))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.EmailAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating own email to the same value then succeeds`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(command(admin, admin))

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `given admin updating own username to the same value then succeeds`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(command(admin, admin))

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `given admin promoting a USER to ADMIN then target role becomes ADMIN`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(admin, target, role = Role.ADMIN))

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Role.ADMIN, updated!!.role)
    }

    @Test
    fun `given admin demoting a USER then target role becomes USER`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", role = Role.ADMIN))

        // When
        val result = handler.handle(command(admin, target, role = Role.USER))

        // Then
        assertTrue(result.isRight())
        val updated = result.fold({ null }, { it })
        assertEquals(Role.USER, updated!!.role)
    }

    @Test
    fun `given admin promoting to SUPERADMIN then returns CannotPromoteToSuperadmin`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(admin, target, role = Role.SUPERADMIN))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.CannotPromoteToSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given non-superadmin admin trying to change a SUPERADMIN's role then returns CannotChangeSuperadminRole`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(command(admin, superadmin, role = Role.USER))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.CannotChangeSuperadminRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin changing own role then returns CannotDemoteYourself`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(command(admin, admin, role = Role.USER))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.CannotDemoteYourself, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given superadmin promoting to SUPERADMIN then returns CannotPromoteToSuperadmin`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(command(superadmin, target, role = Role.SUPERADMIN))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.CannotPromoteToSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given superadmin trying to change another SUPERADMIN's role then returns CannotChangeSuperadminRole`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val otherSuperadmin = userRepository.save(UserFactory.aUser(username = "root2", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(command(superadmin, otherSuperadmin, role = Role.USER))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.CannotChangeSuperadminRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating a non-existent user then returns UserNotFound`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val ghost = User(
            id = User.Id(9999),
            username = Username("ghost"),
            email = Email("ghost@example.com"),
            passwordHash = "encoded_whatever",
            createdAtEpochMs = 0,
            lang = org.ratelog.Lang.en,
            metadataLang = org.ratelog.Lang.en,
            role = Role.USER,
        )

        // When
        val result = handler.handle(command(admin, ghost))

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateUserHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating credentials and role atomically then both are applied`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "oldName", email = "old@example.com", role = Role.USER))

        // When
        val result = handler.handle(
            command(
                admin,
                target,
                username = Username("newName"),
                email = Email("new@example.com"),
                password = Password("NewPass1!"),
                role = Role.ADMIN,
            )
        )

        // Then
        assertTrue(result.isRight())
        val persisted = userRepository.findById(target.id!!)!!
        assertEquals(Username("newName"), persisted.username)
        assertEquals(Email("new@example.com"), persisted.email)
        assertEquals("encoded_NewPass1!", persisted.passwordHash)
        assertEquals(Role.ADMIN, persisted.role)
    }
}