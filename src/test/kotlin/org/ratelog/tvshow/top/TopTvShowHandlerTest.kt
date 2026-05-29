package org.ratelog.tvshow.top

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.InMemoryTvRatingRepository
import org.ratelog.test.TvRatingFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import java.time.Instant

class TopTvShowHandlerTest {

    private lateinit var tvRatingRepository: InMemoryTvRatingRepository
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var handler: TopTvShowHandler

    @BeforeEach
    fun setUp() {
        tvRatingRepository = InMemoryTvRatingRepository()
        tvShowRepository = InMemoryTvShowRepository()
        handler = TopTvShowHandler(tvRatingRepository, tvShowRepository)
    }

    @Test
    fun `should return empty list when user has no ratings`() {
        val query = TopTvShow(User.Id(1), null, 10, null)

        val result = handler.handle(query)

        assertEquals(0, result.size)
    }

    @Test
    fun `should return ranked tv shows when user has ratings`() {
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Show A")
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 456, name = "Show B")
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)

        val rating1 = TvRatingFactory.aTvRating(id = 1, tvShowId = TvShow.Id(1), userId = User.Id(1), createdAt = Instant.now())
        val rating2 = TvRatingFactory.aTvRating(id = 2, tvShowId = TvShow.Id(2), userId = User.Id(1), createdAt = Instant.now())
        tvRatingRepository.save(rating1)
        tvRatingRepository.save(rating2)

        val query = TopTvShow(User.Id(1), null, 10, null)
        val result = handler.handle(query)

        assertEquals(2, result.size)
        assertEquals(1, result[0].rank.value)
        assertEquals(2, result[1].rank.value)
    }

    @Test
    fun `should respect limit parameter`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 123, name = "Show A")
        tvShowRepository.save(show)

        val rating = TvRatingFactory.aTvRating(id = 1, tvShowId = TvShow.Id(1), userId = User.Id(1), createdAt = Instant.now())
        tvRatingRepository.save(rating)

        val query = TopTvShow(User.Id(1), null, 0, null)
        val result = handler.handle(query)

        assertEquals(0, result.size)
    }
}
