package org.raterr.tvshow

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.TmdbId
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbError
import org.raterr.tmdb.TmdbGenre
import org.raterr.tmdb.TmdbTvShow
import org.raterr.tvshow.get.GetTvShow
import org.raterr.tvshow.get.GetTvShowHandler

class GetTvShowHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val tvShowRepository = InMemoryTvShowRepository()
    private val handler = GetTvShowHandler(tmdbClient, tvShowRepository)

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
    }

    @Test
    fun `creates new tvshow when not in repo`() {
        val tmdbShow = TmdbTvShow(
            id = 456,
            name = "Test Show",
            originalName = "Original Show",
            overview = "Show Overview",
            firstAirDate = "2024-03-15",
            posterPath = "/show-poster.jpg",
            voteAverage = 8.2,
            genres = listOf(TmdbGenre(1, "Drama")),
            status = "Running"
        )
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(tmdbShow.right())

        val result = handler.handle(GetTvShow(TmdbId(456)))

        assertTrue(result.isRight())
        val saved = tvShowRepository.findByTmdbId(456).get()
        assertEquals(456, saved.tmdbId)
        assertEquals("Test Show", saved.name)
        assertEquals("Original Show", saved.originalName)
        assertEquals("Show Overview", saved.overview)
        assertEquals("2024-03-15", saved.firstAirDate)
        assertEquals(2024, saved.firstAirYear)
        assertEquals("/show-poster.jpg", saved.posterPath)
        assertEquals(8.2, saved.tmdbVoteAverage)
        assertEquals("Drama", saved.genres)
    }

    @Test
    fun `updates existing tvshow when in repo`() {
        tvShowRepository.save(
            TvShow(
                id = 10,
                tmdbId = 456,
                name = "Old Name",
                originalName = "Old Original",
                overview = "Old Overview",
                firstAirDate = "2020-01-01",
                firstAirYear = 2020,
                posterPath = "/old.jpg",
                tmdbVoteAverage = 6.0,
                genres = "Action"
            )
        )
        val tmdbShow = TmdbTvShow(
            id = 456,
            name = "New Name",
            originalName = "New Original",
            overview = "New Overview",
            firstAirDate = "2024-09-01",
            posterPath = "/new.jpg",
            voteAverage = 9.0,
            genres = listOf(TmdbGenre(3, "Sci-Fi")),
            status = "Ended"
        )
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(tmdbShow.right())

        val result = handler.handle(GetTvShow(TmdbId(456)))

        assertTrue(result.isRight())
        val saved = tvShowRepository.findByTmdbId(456).get()
        assertEquals(10, saved.id)
        assertEquals("New Name", saved.name)
        assertEquals("Sci-Fi", saved.genres)
    }

    @Test
    fun `returns TvShowNotFound when TMDB fails`() {
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(GetTvShow(TmdbId(456)))

        assertTrue(result.isLeft())
    }
}
