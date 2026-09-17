package org.ratelog.admin.updaterole

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Role
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class AdminUpdateRoleHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: AdminUpdateRoleHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = AdminUpdateRoleHandler(userRepository)
    }

    @Test
    fun `given a USER current user when updating role then returns Forbidden`() {
        // Given
        val current = userRepository.save(UserFactory.aUser(username = "normal"))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = current, targetUserId = target.id!!, newRole = Role.ADMIN)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin promoting a USER to ADMIN then target role becomes ADMIN`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = admin, targetUserId = target.id!!, newRole = Role.ADMIN)
        )

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
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = admin, targetUserId = target.id!!, newRole = Role.USER)
        )

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
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = admin, targetUserId = target.id!!, newRole = Role.SUPERADMIN)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.CannotPromoteToSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given non-superadmin admin trying to change a SUPERADMIN's role then returns CannotChangeSuperadminRole`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = admin, targetUserId = superadmin.id!!, newRole = Role.USER)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.CannotChangeSuperadminRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin changing own role then returns CannotDemoteYourself`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = admin, targetUserId = admin.id!!, newRole = Role.USER)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.CannotDemoteYourself, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given superadmin promoting to SUPERADMIN then returns CannotPromoteToSuperadmin`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = superadmin, targetUserId = target.id!!, newRole = Role.SUPERADMIN)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.CannotPromoteToSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given superadmin trying to change another SUPERADMIN's role then returns CannotChangeSuperadminRole`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val otherSuperadmin = userRepository.save(UserFactory.aUser(username = "root2", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = superadmin, targetUserId = otherSuperadmin.id!!, newRole = Role.USER)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.CannotChangeSuperadminRole, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin updating role of a non-existent user then returns UserNotFound`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(
            AdminUpdateRoleCommand(currentUser = admin, targetUserId = User.Id(9999), newRole = Role.ADMIN)
        )

        // Then
        assertTrue(result.isLeft())
        assertEquals(AdminUpdateRoleHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }
}
