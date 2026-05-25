package org.raterr.user.followed

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.user.User
import org.raterr.userfollow.InMemoryUserFollowRepository
import org.raterr.userfollow.UserFollow

class FollowedUsersHandlerTest {

    private val userFollowRepository = InMemoryUserFollowRepository()
    private val handler = FollowedUsersHandler(userFollowRepository)

    @BeforeEach
    fun setUp() {
        userFollowRepository.clear()
    }

    @Test
    fun `returns followed users when user follows other users`() {
        userFollowRepository.addUser(1, "alice")
        userFollowRepository.addUser(2, "bob")
        userFollowRepository.addUser(3, "charlie")

        userFollowRepository.save(UserFollow(followerId = 1, followedId = 2))
        userFollowRepository.save(UserFollow(followerId = 1, followedId = 3))

        val result = handler.handle(FollowedUsersQuery(User.Id(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(2, it.size)
                assertEquals("bob", it[0].username)
                assertEquals("charlie", it[1].username)
            }
        )
    }

    @Test
    fun `returns empty list when user follows nobody`() {
        userFollowRepository.addUser(1, "alice")

        val result = handler.handle(FollowedUsersQuery(User.Id(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertTrue(it.isEmpty())
            }
        )
    }
}
