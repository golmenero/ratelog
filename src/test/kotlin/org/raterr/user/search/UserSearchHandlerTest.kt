package org.raterr.user.search

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.Email
import org.raterr.Username
import org.raterr.user.InMemoryUserRepository
import org.raterr.user.User
import org.raterr.userfollow.InMemoryUserFollowRepository
import org.raterr.userfollow.UserFollow

class UserSearchHandlerTest {

    private val userRepository = InMemoryUserRepository()
    private val userFollowRepository = InMemoryUserFollowRepository()
    private val handler = UserSearchHandler(userRepository, userFollowRepository)

    @BeforeEach
    fun setUp() {
        userRepository.clear()
        userFollowRepository.clear()
    }

    @Test
    fun `returns users when query matches and user is logged in`() {
        userRepository.save(
            User(
                id = null,
                username = Username("alice"),
                email = Email("alice@test.com"),
                passwordHash = "hash",
                createdAtEpochMs = 1700000000000
            )
        )
        userRepository.save(User(id = null, username = Username("bob"), email = Email("bob@test.com"), passwordHash = "hash", createdAtEpochMs = 1700000000000))
        userFollowRepository.addUser(1, "alice")
        userFollowRepository.addUser(2, "bob")

        val result = handler.handle(UserSearchQuery(Username("ali"), User.Id(2)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals("alice", it[0].username.value)
                assertEquals(false, it[0].isFollowed)
            }
        )
    }

    @Test
    fun `returns users with isFollowed true when follower follows them`() {
        userRepository.save(User(id = null, username = Username("alice"), email = Email("alice@test.com"), passwordHash = "hash", createdAtEpochMs = 1700000000000))
        userRepository.save(User(id = null, username = Username("bob"), email = Email("bob@test.com"), passwordHash = "hash", createdAtEpochMs = 1700000000000))
        userFollowRepository.addUser(1, "alice")
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.save(UserFollow(followerId = 2, followedId = 1))

        val result = handler.handle(UserSearchQuery(Username("ali"), User.Id(2)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals(true, it[0].isFollowed)
            }
        )
    }

    @Test
    fun `returns users without isFollowed when no followerId`() {
        userRepository.save(User(id = null, username = Username("alice"), email = Email("alice@test.com"), passwordHash = "hash", createdAtEpochMs = 1700000000000))

        val result = handler.handle(UserSearchQuery(Username("ali"), null))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.size)
                assertEquals(false, it[0].isFollowed)
            }
        )
    }

    @Test
    fun `returns EmptyQuery when query is blank`() {
        val result = handler.handle(UserSearchQuery(Username(""), null))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is UserSearchHandlerError.EmptyQuery) },
            { }
        )
    }

    @Test
    fun `returns NoUsersFound when no users match`() {
        val result = handler.handle(UserSearchQuery(Username("nonexistent"), null))

        assertTrue(result.isLeft())
        result.fold(
            { assertTrue(it is UserSearchHandlerError.NoUsersFound) },
            { }
        )
    }
}
