package org.raterr.tvshow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.Genre
import org.raterr.user.User.Id
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvrating.aTvRating
import org.raterr.tvshow.top.TopTvShow

class TopTvShowHandlerTest {

    private val tvShowRepository = InMemoryTvShowRepository()
    private val tvRatingRepository = InMemoryTvRatingRepository(tvShowRepository)
    private lateinit var handler: org.raterr.tvshow.top.TopTvShowHandler

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
        tvRatingRepository.clear()
        handler = org.raterr.tvshow.top.TopTvShowHandler(tvShowRepository, tvRatingRepository)
    }

    @Test
    fun `no filters returns ratings with shows ordered by score`() {
        val show1 = tvShowRepository.save(aTvShow(id = TvShow.Id(1), tmdbId = 100, name = "Show1"))
        val show2 = tvShowRepository.save(aTvShow(id = TvShow.Id(2), tmdbId = 200, name = "Show2"))
        tvRatingRepository.save(
            aTvRating(
                tvShowId = show1.id!!,
                userId = Id(1),
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0
            )
        )
        tvRatingRepository.save(
            aTvRating(
                tvShowId = show2.id!!,
                userId = Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        val result = handler.handle(TopTvShow(Id(1), null, 10, null))

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank.value)
        assertEquals(2, result[1].rating.rank.value)
        assertEquals("Show1", result[0].show.name.value)
    }

    @Test
    fun `filters by category keeps absolute rank`() {
        val show1 = tvShowRepository.save(aTvShow(id = TvShow.Id(1), tmdbId = 100, name = "DramaShow", genres = listOf(Genre.Drama)))
        val show2 = tvShowRepository.save(aTvShow(id = TvShow.Id(2), tmdbId = 200, name = "ComedyShow", genres = listOf(Genre.Comedy)))
        val show3 = tvShowRepository.save(aTvShow(id = TvShow.Id(3), tmdbId = 300, name = "AnotherDrama", genres = listOf(Genre.Drama)))
        tvRatingRepository.save(
            aTvRating(
                tvShowId = show1.id!!,
                userId = Id(1),
                directing = 10.0,
                cinematography = 10.0,
                acting = 10.0,
                soundtrack = 10.0,
                screenplay = 10.0
            )
        )
        tvRatingRepository.save(
            aTvRating(
                tvShowId = show2.id!!,
                userId = Id(1),
                directing = 8.0,
                cinematography = 8.0,
                acting = 8.0,
                soundtrack = 8.0,
                screenplay = 8.0
            )
        )
        tvRatingRepository.save(
            aTvRating(
                tvShowId = show3.id!!,
                userId = Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        val result = handler.handle(TopTvShow(Id(1), "Drama", 10, null))

        assertEquals(2, result.size)
        assertEquals(1, result[0].rating.rank.value)
        assertEquals(3, result[1].rating.rank.value)
    }

    @Test
    fun `filters by name keeps absolute rank`() {
        val show = tvShowRepository.save(aTvShow(tmdbId = 100, name = "Breaking Bad"))
        tvRatingRepository.save(
            aTvRating(
                tvShowId = show.id!!,
                userId = Id(1),
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0
            )
        )

        val result = handler.handle(TopTvShow(Id(1), null, 10, "Breaking"))

        assertEquals(1, result.size)
        assertEquals(1, result[0].rating.rank.value)
    }

    @Test
    fun `limits results`() {
        val result = handler.handle(TopTvShow(Id(1), null, 3, null))

        assertEquals(0, result.size)
    }

    @Test
    fun `empty returns empty list`() {
        val result = handler.handle(TopTvShow(Id(1), null, 10, null))

        assertEquals(0, result.size)
    }
}
