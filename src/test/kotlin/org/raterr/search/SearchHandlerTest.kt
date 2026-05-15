package org.raterr.search

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.raterr.MediaType
import org.raterr.UserId
import org.raterr.movie.Movie
import org.raterr.rating.Rating
import org.raterr.follow.InMemoryFollowRepository
import org.raterr.movie.InMemoryMovieRepository
import org.raterr.rating.InMemoryRatingRepository
import org.raterr.tvrating.InMemoryTvRatingRepository
import org.raterr.tvshow.InMemoryTvShowRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.tmdb.TmdbError
import org.raterr.tmdb.TmdbMovie
import org.raterr.tmdb.TmdbTvShow
import org.raterr.tvrating.TvRating
import java.time.LocalDate

class SearchHandlerTest {

    private val tmdbClient: TmdbClient = mock()
    private val tvShowRepository = InMemoryTvShowRepository()
    private val movieRepository = InMemoryMovieRepository()
    private val ratingRepository = InMemoryRatingRepository()
    private val tvRatingRepository = InMemoryTvRatingRepository()
    private val followRepository = InMemoryFollowRepository()
    private val handler = SearchHandler(tmdbClient, tvShowRepository, movieRepository, ratingRepository, tvRatingRepository, followRepository)

    @BeforeEach
    fun setUp() {
        tvShowRepository.clear()
        movieRepository.clear()
        ratingRepository.clear()
        tvRatingRepository.clear()
        followRepository.clear()
    }

    @Test
    fun `happy path returns interleaved results`() {
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")
        val tmdbShow = TmdbTvShow(id = 2, name = "Show", firstAirDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(listOf(tmdbShow).right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(2, items.size)
        assertEquals("Movie", items[0].title)
        assertEquals("Show", items[1].title)
    }

    @Test
    fun `only movies returns movie results`() {
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(1, items.size)
        assertEquals(MediaType.movie.name, items[0].type)
    }

    @Test
    fun `only tvshows returns show results`() {
        val tmdbShow = TmdbTvShow(id = 2, name = "Show", firstAirDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(emptyList<TmdbMovie>().right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(listOf(tmdbShow).right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(1, items.size)
        assertEquals(MediaType.tvshow.name, items[0].type)
    }

    @Test
    fun `interleaves 3 movies and 2 shows correctly`() {
        val movies = listOf(
            TmdbMovie(id = 1, title = "M1", releaseDate = "2024-01-01"),
            TmdbMovie(id = 2, title = "M2", releaseDate = "2024-01-01"),
            TmdbMovie(id = 3, title = "M3", releaseDate = "2024-01-01")
        )
        val shows = listOf(
            TmdbTvShow(id = 10, name = "S1", firstAirDate = "2024-01-01"),
            TmdbTvShow(id = 11, name = "S2", firstAirDate = "2024-01-01")
        )
        whenever(tmdbClient.searchMovies("test")).thenReturn(movies.right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(shows.right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(5, items.size)
        assertEquals("M1", items[0].title)
        assertEquals("S1", items[1].title)
        assertEquals("M2", items[2].title)
        assertEquals("S2", items[3].title)
        assertEquals("M3", items[4].title)
    }

    @Test
    fun `blank query returns empty list`() {
        val result = handler.handle(SearchQuery("", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(0, items.size)
    }

    @Test
    fun `isFollowed true when user follows movie`() {
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())
        followRepository.save(
            org.raterr.follow.Follow(
                userId = 1,
                contentType = MediaType.movie.name,
                contentTmdbId = 1,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(true, items[0].isFollowed)
    }

    @Test
    fun `canRate false for future release date`() {
        val futureDate = LocalDate.now().plusYears(1).toString()
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = futureDate)
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(false, items[0].canRate)
    }

    @Test
    fun `canRate true for past release date`() {
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2020-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(true, items[0].canRate)
    }

    @Test
    fun `canFollow false when rating exists`() {
        val movie = movieRepository.save(Movie(id = 5, tmdbId = 1, title = "Movie"))
        ratingRepository.save(
            Rating(
                id = 1,
                movieId = movie.id!!,
                userId = 1,
                directing = 5.0,
                cinematography = 5.0,
                acting = 5.0,
                soundtrack = 5.0,
                screenplay = 5.0,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(false, items[0].canFollow)
    }

    @Test
    fun `canFollow true when no rating exists`() {
        movieRepository.save(Movie(id = 5, tmdbId = 1, title = "Movie"))
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(true, items[0].canFollow)
    }

    @Test
    fun `userId null means isFollowed false`() {
        val tmdbMovie = TmdbMovie(id = 1, title = "Movie", releaseDate = "2024-01-01")
        whenever(tmdbClient.searchMovies("test")).thenReturn(listOf(tmdbMovie).right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", null))

        assertTrue(result.isRight())
        val items = (result as arrow.core.Either.Right).value
        assertEquals(false, items[0].isFollowed)
    }

    @Test
    fun `TMDB error on movies returns Left`() {
        whenever(tmdbClient.searchMovies("test")).thenReturn(TmdbError.MovieNotFound.left())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(emptyList<TmdbTvShow>().right())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isLeft())
    }

    @Test
    fun `TMDB error on tvshows returns Left`() {
        whenever(tmdbClient.searchMovies("test")).thenReturn(emptyList<TmdbMovie>().right())
        whenever(tmdbClient.searchTvShows("test")).thenReturn(TmdbError.TvShowNotFound.left())

        val result = handler.handle(SearchQuery("test", UserId(1)))

        assertTrue(result.isLeft())
    }
}
