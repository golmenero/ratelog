package org.ratelog.user.followed

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class FollowedUsersHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: FollowedUsersHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = FollowedUsersHandler(userRepository)
    }

    @Test
    fun `should return empty list when user follows no one`() {
        val query = FollowedUsersQuery(User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isRight())
        assertEquals(0, result.getOrElse { emptyList() }.size)
    }

    @Test
    fun `should return list of followed users`() {
        val mainUser = UserFactory.aUser(id = 1, username = "mainuser", email = "main@example.com")
        val followedUser1 = UserFactory.aUser(id = 2, username = "user1", email = "user1@example.com", followed = true)
        val followedUser2 = UserFactory.aUser(id = 3, username = "user2", email = "user2@example.com", followed = true)
        userRepository.save(mainUser)
        userRepository.save(followedUser1)
        userRepository.save(followedUser2)

        val query = FollowedUsersQuery(User.Id(1))
        val result = handler.handle(query)

        assertTrue(result.isRight())
        val followed = result.getOrElse { emptyList() }
        assertEquals(2, followed.size)
        assertEquals("user1", followed[0].username)
        assertEquals("user2", followed[1].username)
    }
}
