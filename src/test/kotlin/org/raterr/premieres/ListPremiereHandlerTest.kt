package org.raterr.premieres

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbError
import org.raterr.tmdb.TmdbMovie
import org.raterr.tmdb.TmdbTvShow
import java.time.LocalDate

class ListPremiereHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val followRepository = InMemoryFollowRepository()
    private val handler = ListPremiereHandler(tmdbClient, followRepository)

    @BeforeEach
    fun setUp() {
        followRepository.clear()
    }

    @Test
    fun `no follows returns empty premieres`() {
        val result = handler.handle(ListPremiere(UserId(1)))

        assertTrue(result.isRight())
        val premieres = (result as arrow.core.Either.Right).value
        assertEquals(0, premieres.released.size)
        assertEquals(0, premieres.upcoming.size)
        assertEquals(0, premieres.noDate.size)
    }

    @Test
    fun `all released follows go to released list sorted by date`() {
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 1))
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 2))
        whenever(tmdbClient.movieDetails(1)).thenReturn(
            TmdbMovie(id = 1, title = "Movie1", releaseDate = "2024-01-01").right()
        )
        whenever(tmdbClient.movieDetails(2)).thenReturn(
            TmdbMovie(id = 2, title = "Movie2", releaseDate = "2023-06-01").right()
        )

        val result = handler.handle(ListPremiere(UserId(1)))

        assertTrue(result.isRight())
        val premieres = (result as arrow.core.Either.Right).value
        assertEquals(2, premieres.released.size)
        assertEquals("Movie2", premieres.released[0].title)
        assertEquals("Movie1", premieres.released[1].title)
        assertEquals(0, premieres.upcoming.size)
    }

    @Test
    fun `all upcoming follows go to upcoming list sorted by date`() {
        val futureDate = LocalDate.now().plusMonths(6).toString()
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 1))
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 2))
        whenever(tmdbClient.tvShowDetails(1)).thenReturn(
            TmdbTvShow(id = 1, name = "Show1", firstAirDate = futureDate).right()
        )
        whenever(tmdbClient.tvShowDetails(2)).thenReturn(
            TmdbTvShow(id = 2, name = "Show2", firstAirDate = LocalDate.now().plusMonths(3).toString()).right()
        )

        val result = handler.handle(ListPremiere(UserId(1)))

        assertTrue(result.isRight())
        val premieres = (result as arrow.core.Either.Right).value
        assertEquals(2, premieres.upcoming.size)
        assertEquals("Show2", premieres.upcoming[0].title)
        assertEquals("Show1", premieres.upcoming[1].title)
        assertEquals(0, premieres.released.size)
    }

    @Test
    fun `mix of released upcoming and no date categorized correctly`() {
        val pastDate = "2023-01-01"
        val futureDate = LocalDate.now().plusMonths(6).toString()
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 1))
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 2))
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 3))
        whenever(tmdbClient.movieDetails(1)).thenReturn(
            TmdbMovie(id = 1, title = "Released Movie", releaseDate = pastDate).right()
        )
        whenever(tmdbClient.tvShowDetails(2)).thenReturn(
            TmdbTvShow(id = 2, name = "Upcoming Show", firstAirDate = futureDate).right()
        )
        whenever(tmdbClient.movieDetails(3)).thenReturn(
            TmdbMovie(id = 3, title = "No Date Movie", releaseDate = null).right()
        )

        val result = handler.handle(ListPremiere(UserId(1)))

        assertTrue(result.isRight())
        val premieres = (result as arrow.core.Either.Right).value
        assertEquals(1, premieres.released.size)
        assertEquals("Released Movie", premieres.released[0].title)
        assertEquals(1, premieres.upcoming.size)
        assertEquals("Upcoming Show", premieres.upcoming[0].title)
        assertEquals(1, premieres.noDate.size)
        assertEquals("No Date Movie", premieres.noDate[0].title)
        assertEquals(false, premieres.noDate[0].hasDate)
    }

    @Test
    fun `MovieNotFound returns Left`() {
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 1))
        whenever(tmdbClient.movieDetails(1)).thenReturn(TmdbError.MovieNotFound.left())

        val result = handler.handle(ListPremiere(UserId(1)))

        assertTrue(result.isLeft())
    }

    @Test
    fun `TvShowNotFound returns Left`() {
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 1))
        whenever(tmdbClient.tvShowDetails(1)).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(ListPremiere(UserId(1)))

        assertTrue(result.isLeft())
    }
}
