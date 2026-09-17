package org.ratelog.user.listusers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Role
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class ListUsersHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: ListUsersHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = ListUsersHandler(userRepository)
    }

    @Test
    fun `given a USER current user when listing then returns Forbidden`() {
        // Given
        val current = userRepository.save(UserFactory.aUser(username = "normal"))

        // When
        val result = handler.handle(ListUsersQuery(currentUser = current))

        // Then
        assertTrue(result.isLeft())
        assertEquals(ListUsersHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an ADMIN current user when listing then returns all users`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        userRepository.save(UserFactory.aUser(username = "user2"))

        // When
        val result = handler.handle(ListUsersQuery(currentUser = admin))

        // Then
        assertTrue(result.isRight())
        assertEquals(2, result.fold({ emptyList<User>() }, { it }).size)
    }

    @Test
    fun `given a SUPERADMIN current user when listing then returns all users`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        userRepository.save(UserFactory.aUser(username = "user1"))

        // When
        val result = handler.handle(ListUsersQuery(currentUser = superadmin))

        // Then
        assertTrue(result.isRight())
        assertEquals(2, result.fold({ emptyList<User>() }, { it }).size)
    }
}