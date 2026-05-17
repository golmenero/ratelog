package org.raterr.userfollow

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.user.User
import org.raterr.user.InMemoryUserRepository
import org.raterr.userfollow.toggleuser.ToggleUserFollow
import org.raterr.userfollow.toggleuser.ToggleUserFollowHandler
import org.raterr.userfollow.toggleuser.ToggleUserFollowHandlerError

class ToggleUserFollowHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val userFollowRepository = InMemoryUserFollowRepository()
    private val handler = ToggleUserFollowHandler(userFollowRepository, userRepository)

    @BeforeEach
    fun setUp() {
        userRepository.clear()
        userFollowRepository.clear()
    }

    @Test
    fun `follows user successfully when user exists and not already following`() {
        userRepository.save(User(id = 2, username = "followed", email = "followed@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000))
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
        val result = handler.handle(ToggleUserFollow(UserId(1), "nonexistent"))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is ToggleUserFollowHandlerError.UserNotFound) },
            { }
        )
    }

    @Test
    fun `returns CannotFollowYourself when user tries to follow themselves`() {
        userRepository.save(User(id = 1, username = "self", email = "self@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000))

        val result = handler.handle(ToggleUserFollow(UserId(1), "self"))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is ToggleUserFollowHandlerError.CannotFollowYourself) },
            { }
        )
    }

    @Test
    fun `unfollows when already following`() {
        userRepository.save(User(id = 2, username = "followed", email = "followed@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000))
        userFollowRepository.addUser(2, "followed")
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))

        val result = handler.handle(ToggleUserFollow(UserId(1), "followed"))

        assertTrue(result.isRight())
        val follows = userFollowRepository.findFollowingByUserId(1)
        assertTrue(follows.isEmpty())
    }

    @Test
    fun `toggle twice results in no follow`() {
        userRepository.save(User(id = 2, username = "followed", email = "followed@test.com", passwordHash = "hash", createdAtEpochMs = 1700000000000))
        userFollowRepository.addUser(2, "followed")

        handler.handle(ToggleUserFollow(UserId(1), "followed"))
        handler.handle(ToggleUserFollow(UserId(1), "followed"))

        val follows = userFollowRepository.findFollowingByUserId(1)
        assertTrue(follows.isEmpty())
    }
}
