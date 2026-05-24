package org.raterr.tvshow

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.Genre
import org.raterr.Overview
import org.raterr.Title
import org.raterr.TmdbId
import org.raterr.Url
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
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(TmdbTvShow(
            id = 456,
            name = "Test Show",
            originalName = "Original Show",
            overview = "Show Overview",
            firstAirDate = "2024-03-15",
            posterPath = "/show-poster.jpg",
            voteAverage = 8.2,
            genres = listOf(TmdbGenre(1, "Drama")),
            status = "Running"
        ).right())

        val result = handler.handle(GetTvShow(TmdbId(456)))

        assertTrue(result.isRight())
        val saved = tvShowRepository.findByTmdbId(TmdbId(456))!!
        assertEquals(456, saved.tmdbId.value)
        assertEquals("Test Show", saved.name.value)
        assertEquals("Original Show", saved.originalName?.value)
        assertEquals("Show Overview", saved.overview?.value)
        assertEquals("2024-03-15", saved.firstAirDate.toString())
        assertEquals(2024, saved.firstAirYear)
        assertEquals("/show-poster.jpg", saved.posterPath?.value)
        assertEquals(8.2, saved.tmdbVoteAverage)
        assertEquals(listOf(org.raterr.Genre.Drama), saved.genres)
    }

    @Test
    fun `updates existing tvshow when in repo`() {
        tvShowRepository.save(
            TvShow(
                id = TvShow.Id(10),
                tmdbId = TmdbId(456),
                name = Title("Old Name"),
                originalName = Title("Old Original"),
                overview = Overview("Old Overview"),
                firstAirDate = java.time.LocalDate.parse("2020-01-01"),
                firstAirYear = 2020,
                posterPath = Url("/old.jpg"),
                tmdbVoteAverage = 6.0,
                genres = listOf(Genre.Action)
            )
        )
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(TmdbTvShow(
            id = 456,
            name = "New Name",
            originalName = "New Original",
            overview = "New Overview",
            firstAirDate = "2024-09-01",
            posterPath = "/new.jpg",
            voteAverage = 9.0,
            genres = listOf(TmdbGenre(3, "Action")),
            status = "Ended"
        ).right())

        val result = handler.handle(GetTvShow(TmdbId(456)))

        assertTrue(result.isRight())
        val saved = tvShowRepository.findByTmdbId(TmdbId(456))!!
        assertEquals(10, saved.id?.value)
        assertEquals("New Name", saved.name.value)
        assertEquals(listOf(Genre.Action), saved.genres)
    }

    @Test
    fun `returns TvShowNotFound when TMDB fails`() {
        whenever(tmdbClient.tvShowDetails(456)).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(GetTvShow(TmdbId(456)))

        assertTrue(result.isLeft())
    }
}
