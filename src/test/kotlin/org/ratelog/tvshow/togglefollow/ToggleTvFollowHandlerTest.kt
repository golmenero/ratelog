package org.ratelog.tvshow.togglefollow

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User

class ToggleTvFollowHandlerTest {

    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var handler: ToggleTvFollowHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        handler = ToggleTvFollowHandler(tvShowRepository)
    }

    @Test
    fun `should follow tv show successfully`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Test Show")
        tvShowRepository.save(show)

        val command = ToggleTvFollow(TvShow.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertTrue(tvShowRepository.isFollowed(User.Id(1), TvShow.Id(1)))
    }

    @Test
    fun `should return TvShowNotFound when show does not exist`() {
        val command = ToggleTvFollow(TvShow.Id(999), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(ToggleTvFollowHandlerError.TvShowNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should unfollow tv show when already followed`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Test Show")
        tvShowRepository.save(show)
        tvShowRepository.toggleFollow(User.Id(1), TvShow.Id(1))

        val command = ToggleTvFollow(TvShow.Id(1), User.Id(1))

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertFalse(tvShowRepository.isFollowed(User.Id(1), TvShow.Id(1)))
    }
}
