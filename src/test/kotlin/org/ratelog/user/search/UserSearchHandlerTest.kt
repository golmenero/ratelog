package org.ratelog.user.search

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Username
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class UserSearchHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: UserSearchHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = UserSearchHandler(userRepository)
    }

    @Test
    fun `should return EmptyQuery error when username is blank`() {
        val query = UserSearchQuery(Username(""), null)

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertEquals(UserSearchHandlerError.EmptyQuery, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return NoUsersFound when no users match`() {
        val query = UserSearchQuery(Username("nonexistent"), null)

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertTrue(result.fold({ it is UserSearchHandlerError.NoUsersFound }, { false }))
    }

    @Test
    fun `should return list of users when users match`() {
        val user1 = UserFactory.aUser(id = 1, username = "testuser", email = "test@example.com")
        val user2 = UserFactory.aUser(id = 2, username = "testuser2", email = "test2@example.com")
        userRepository.save(user1)
        userRepository.save(user2)

        val query = UserSearchQuery(Username("test"), null)
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(2, results.size)
        assertEquals("testuser", results[0].username.value)
        assertEquals("testuser2", results[1].username.value)
    }

    @Test
    fun `should return users with follower context when followerId is provided`() {
        val follower = UserFactory.aUser(id = 99, username = "follower", email = "follower@example.com")
        val user = UserFactory.aUser(id = 1, username = "testuser", email = "test@example.com")
        userRepository.save(follower)
        userRepository.save(user)

        val query = UserSearchQuery(Username("test"), User.Id(99))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val results = result.getOrElse { emptyList() }
        assertEquals(1, results.size)
    }
}
