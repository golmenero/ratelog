package org.ratelog.admin.createuser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Password
import org.ratelog.Role
import org.ratelog.Username
import org.ratelog.test.FakePasswordEncoder
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory

class AdminCreateUserHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var passwordEncoder: FakePasswordEncoder
    private lateinit var handler: AdminCreateUserHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        passwordEncoder = FakePasswordEncoder()
        handler = AdminCreateUserHandler(userRepository, passwordEncoder)
    }

    @Test
    fun `given a USER current user when creating then returns Forbidden`() {
        // Given
        val current = userRepository.save(UserFactory.aUser(username = "normal"))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = current,
                username = Username("newuser"),
                email = Email("new@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.USER,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminCreateUserHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an ADMIN current user creating a USER then user is created`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = admin,
                username = Username("newuser"),
                email = Email("new@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.USER,
            )
        )

        // Then
        assertTrue(result.isRight())
        val saved = userRepository.findByUsername(Username("newuser"))
        assertNotNull(saved)
        assertEquals(Role.USER, saved!!.role)
    }

    @Test
    fun `given an ADMIN current user creating an ADMIN then user is created with ADMIN role`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = admin,
                username = Username("newadmin"),
                email = Email("admin@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.ADMIN,
            )
        )

        // Then
        assertTrue(result.isRight())
        val saved = userRepository.findByUsername(Username("newadmin"))
        assertEquals(Role.ADMIN, saved!!.role)
    }

    @Test
    fun `given an ADMIN current user trying to create SUPERADMIN then returns CannotPromoteToSuperadmin`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = admin,
                username = Username("root"),
                email = Email("root@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.SUPERADMIN,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminCreateUserHandlerError.CannotPromoteToSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a SUPERADMIN current user creating another SUPERADMIN then user is created`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = superadmin,
                username = Username("root2"),
                email = Email("root2@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.SUPERADMIN,
            )
        )

        // Then
        assertTrue(result.isRight())
        assertEquals(Role.SUPERADMIN, userRepository.findByUsername(Username("root2"))!!.role)
    }

    @Test
    fun `given an existing username when creating then returns UsernameAlreadyExists`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        userRepository.save(UserFactory.aUser(username = "taken"))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = admin,
                username = Username("taken"),
                email = Email("fresh@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.USER,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminCreateUserHandlerError.UsernameAlreadyExists, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an existing email when creating then returns EmailAlreadyExists`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        userRepository.save(UserFactory.aUser(username = "u1", email = "dupe@example.com"))

        // When
        val result = handler.handle(
            AdminCreateUserCommand(
                currentUser = admin,
                username = Username("fresh"),
                email = Email("dupe@example.com"),
                password = Password("Password1!"),
                lang = Lang.en,
                role = Role.USER,
            )
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminCreateUserHandlerError.EmailAlreadyExists, result.fold({ it }, { Unit }))
    }
}
