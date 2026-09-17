package org.ratelog.user.delete

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Role
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class DeleteUserHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: DeleteUserHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = DeleteUserHandler(userRepository)
    }

    @Test
    fun `given a USER current user when deleting then returns Forbidden`() {
        // Given
        val current = userRepository.save(UserFactory.aUser(username = "normal"))
        val target = userRepository.save(UserFactory.aUser(username = "target"))

        // When
        val result = handler.handle(DeleteUserCommand(currentUser = current, targetUserId = target.id!!))

        // Then
        assertTrue(result.isLeft())
        assertEquals(DeleteUserHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin deleting another user then user is removed`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))

        // When
        val result = handler.handle(DeleteUserCommand(currentUser = admin, targetUserId = target.id!!))

        // Then
        assertTrue(result.isRight())
        assertNull(userRepository.findById(target.id!!))
    }

    @Test
    fun `given admin deleting themselves then returns CannotDeleteYourself`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(DeleteUserCommand(currentUser = admin, targetUserId = admin.id!!))

        // Then
        assertTrue(result.isLeft())
        assertEquals(DeleteUserHandlerError.CannotDeleteYourself, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given non-superadmin admin deleting a SUPERADMIN then returns CannotDeleteSuperadmin`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(DeleteUserCommand(currentUser = admin, targetUserId = superadmin.id!!))

        // Then
        assertTrue(result.isLeft())
        assertEquals(DeleteUserHandlerError.CannotDeleteSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given superadmin deleting a SUPERADMIN then returns CannotDeleteSuperadmin`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root1", role = Role.SUPERADMIN))
        val other = userRepository.save(UserFactory.aUser(username = "root2", role = Role.SUPERADMIN))

        // When
        val result = handler.handle(DeleteUserCommand(currentUser = superadmin, targetUserId = other.id!!))

        // Then
        assertTrue(result.isLeft())
        assertEquals(DeleteUserHandlerError.CannotDeleteSuperadmin, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given admin deleting a non-existent user then returns UserNotFound`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))

        // When
        val result = handler.handle(DeleteUserCommand(currentUser = admin, targetUserId = User.Id(9999)))

        // Then
        assertTrue(result.isLeft())
        assertEquals(DeleteUserHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }
}