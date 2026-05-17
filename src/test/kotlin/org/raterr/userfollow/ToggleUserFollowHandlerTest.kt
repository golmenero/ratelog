package org.raterr.userfollow

import arrow.core.Either
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.UserId
import org.raterr.userfollow.toggleuser.ToggleUserFollow
import org.raterr.userfollow.toggleuser.ToggleUserFollowHandler
import org.raterr.userfollow.toggleuser.ToggleUserFollowHandlerError
import org.raterr.user.User
import org.raterr.user.UserRepository
import java.util.Optional

class ToggleUserFollowHandlerTest {

    private val userRepository: UserRepository = mock()
    private val userFollowRepository = InMemoryUserFollowRepository()
    private val handler = ToggleUserFollowHandler(userFollowRepository, userRepository)

    @BeforeEach
    fun setUp() {
        userFollowRepository.clear()
    }

    @Test
    fun `follows user successfully when user exists and not already following`() {
        val follower = User(id = 1, username = "follower", email = "follower@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        val followed = User(id = 2, username = "followed", email = "followed@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findByUsername("followed")).thenReturn(Optional.of(followed))

        userFollowRepository.addUser(2, "followed")

        val result = handler.handle(ToggleUserFollow(UserId(1), "followed"))

        assertTrue(result.isRight())
        val follows = userFollowRepository.findFollowingByUserId(1)
        assertTrue(follows.size == 1)
        assertTrue(follows[0].followerId == 1L)
        assertTrue(follows[0].followedId == 2L)
    }

    @Test
    fun `returns UserNotFound when target user does not exist`() {
        whenever(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty())

        val result = handler.handle(ToggleUserFollow(UserId(1), "nonexistent"))

        assertTrue(result.isLeft())
        val error = (result as Either.Left).value
        assertTrue(error is ToggleUserFollowHandlerError.UserNotFound)
    }

    @Test
    fun `returns CannotFollowYourself when user tries to follow themselves`() {
        val user = User(id = 1, username = "self", email = "self@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findByUsername("self")).thenReturn(Optional.of(user))

        val result = handler.handle(ToggleUserFollow(UserId(1), "self"))

        assertTrue(result.isLeft())
        val error = (result as Either.Left).value
        assertTrue(error is ToggleUserFollowHandlerError.CannotFollowYourself)
    }

    @Test
    fun `unfollows when already following`() {
        val follower = User(id = 1, username = "follower", email = "follower@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        val followed = User(id = 2, username = "followed", email = "followed@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findByUsername("followed")).thenReturn(Optional.of(followed))

        userFollowRepository.addUser(2, "followed")
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))

        val result = handler.handle(ToggleUserFollow(UserId(1), "followed"))

        assertTrue(result.isRight())
        val follows = userFollowRepository.findFollowingByUserId(1)
        assertTrue(follows.isEmpty())
    }

    @Test
    fun `toggle twice results in no follow`() {
        val follower = User(id = 1, username = "follower", email = "follower@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        val followed = User(id = 2, username = "followed", email = "followed@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000)
        whenever(userRepository.findByUsername("followed")).thenReturn(Optional.of(followed))

        userFollowRepository.addUser(2, "followed")

        handler.handle(ToggleUserFollow(UserId(1), "followed"))
        handler.handle(ToggleUserFollow(UserId(1), "followed"))

        val follows = userFollowRepository.findFollowingByUserId(1)
        assertTrue(follows.isEmpty())
    }
}
