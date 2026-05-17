package org.raterr.movie.premieres

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.UserId
import org.raterr.follow.Follow
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.tmdb.FakeTmdbClient
import org.raterr.tmdb.TmdbMovie
import java.time.LocalDate

class MoviePremieresHandlerTest {

    private val followRepository = InMemoryFollowRepository()
    private lateinit var tmdbClient: FakeTmdbClient
    private lateinit var handler: MoviePremieresHandler

    private val today = LocalDate.now()

    @BeforeEach
    fun setUp() {
        followRepository.clear()
    }

    @Test
    fun `returns empty premieres when no follows`() {
        tmdbClient = FakeTmdbClient()
        handler = MoviePremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(MoviePremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertTrue(it.released.isEmpty())
                assertTrue(it.upcoming.isEmpty())
                assertTrue(it.noDate.isEmpty())
            }
        )
    }

    @Test
    fun `returns released movies when release date is in the past`() {
        val pastDate = today.minusDays(5).toString()
        tmdbClient = FakeTmdbClient(movies = mapOf(
            100 to TmdbMovie(id = 100, title = "Past Movie", releaseDate = pastDate, posterPath = "/poster.jpg")
        ))
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 100, createdAtEpochMs = System.currentTimeMillis()))
        handler = MoviePremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(MoviePremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.released.size)
                assertEquals("Past Movie", it.released[0].title)
                assertTrue(it.upcoming.isEmpty())
            }
        )
    }

    @Test
    fun `returns upcoming movies when release date is in the future`() {
        val futureDate = today.plusDays(30).toString()
        tmdbClient = FakeTmdbClient(movies = mapOf(
            101 to TmdbMovie(id = 101, title = "Future Movie", releaseDate = futureDate)
        ))
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 101, createdAtEpochMs = System.currentTimeMillis()))
        handler = MoviePremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(MoviePremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.upcoming.size)
                assertEquals("Future Movie", it.upcoming[0].title)
                assertTrue(it.released.isEmpty())
            }
        )
    }

    @Test
    fun `returns noDate movies when release date is null`() {
        tmdbClient = FakeTmdbClient(movies = mapOf(
            102 to TmdbMovie(id = 102, title = "TBA Movie", releaseDate = null)
        ))
        followRepository.save(Follow(userId = 1, contentType = "movie", contentTmdbId = 102, createdAtEpochMs = System.currentTimeMillis()))
        handler = MoviePremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(MoviePremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertEquals(1, it.noDate.size)
                assertEquals("TBA Movie", it.noDate[0].title)
                assertEquals(false, it.noDate[0].hasDate)
            }
        )
    }

    @Test
    fun `filters out tvshow follows`() {
        tmdbClient = FakeTmdbClient()
        followRepository.save(Follow(userId = 1, contentType = "tvshow", contentTmdbId = 200, createdAtEpochMs = System.currentTimeMillis()))
        handler = MoviePremieresHandler(tmdbClient, followRepository)

        val result = handler.handle(MoviePremieresQuery(UserId(1)))

        assertTrue(result.isRight())
        result.fold(
            { },
            {
                assertTrue(it.released.isEmpty())
                assertTrue(it.upcoming.isEmpty())
                assertTrue(it.noDate.isEmpty())
            }
        )
    }
}
