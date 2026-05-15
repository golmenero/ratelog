package org.raterr.follow

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.MediaType
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.follow.toggle.ToggleFollow
import org.raterr.follow.toggle.ToggleFollowHandler

class ToggleFollowHandlerTest {

    private val followRepository = InMemoryFollowRepository()
    private val handler = ToggleFollowHandler(followRepository)

    @BeforeEach
    fun setUp() {
        followRepository.clear()
    }

    @Test
    fun `follows when no existing follow`() {
        handler.handle(ToggleFollow(TmdbId(100), UserId(1), MediaType.movie))

        val follows = followRepository.findByUserId(1)
        Assertions.assertEquals(1, follows.size)
        Assertions.assertEquals(1, follows[0].userId)
        Assertions.assertEquals("movie", follows[0].contentType)
        Assertions.assertEquals(100, follows[0].contentTmdbId)
    }

    @Test
    fun `unfollows when existing follow exists`() {
        followRepository.save(
            Follow(
                userId = 1,
                contentType = "movie",
                contentTmdbId = 100,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        handler.handle(ToggleFollow(TmdbId(100), UserId(1), MediaType.movie))

        val follows = followRepository.findByUserId(1)
        Assertions.assertTrue(follows.isEmpty())
    }

    @Test
    fun `toggle twice results in no follow`() {
        handler.handle(ToggleFollow(TmdbId(200), UserId(1), MediaType.tvshow))
        handler.handle(ToggleFollow(TmdbId(200), UserId(1), MediaType.tvshow))

        val follows = followRepository.findByUserId(1)
        Assertions.assertTrue(follows.isEmpty())
    }
}