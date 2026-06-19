package org.ratelog.user.togglefollow

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Username
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class ToggleUserFollowHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: ToggleUserFollowHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = ToggleUserFollowHandler(userRepository)
    }

    @Test
    fun `should follow user successfully`() {
        val userToFollow = UserFactory.aUser(id = 2, username = "targetuser", email = "target@example.com")
        userRepository.save(userToFollow)

        val command = ToggleUserFollow(User.Id(1), User.Id(2))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertTrue(userRepository.isFollowing(User.Id(1), User.Id(2)))
    }

    @Test
    fun `should return UserNotFound when target user does not exist`() {
        val command = ToggleUserFollow(User.Id(1), User.Id(2))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(ToggleUserFollowHandlerError.UserNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return CannotFollowYourself when trying to follow yourself`() {
        val user = UserFactory.aUser(id = 1, username = "myself", email = "myself@example.com")
        userRepository.save(user)

        val command = ToggleUserFollow(User.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(ToggleUserFollowHandlerError.CannotFollowYourself, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should unfollow user when already followed`() {
        val userToFollow = UserFactory.aUser(id = 2, username = "targetuser", email = "target@example.com")
        userRepository.save(userToFollow)
        userRepository.toggleFollow(User.Id(1), User.Id(2))

        val command = ToggleUserFollow(User.Id(1), User.Id(2))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertFalse(userRepository.isFollowing(User.Id(1), User.Id(2)))
    }
}
