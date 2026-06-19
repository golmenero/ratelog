package org.ratelog.tmdb

import arrow.core.left
import arrow.core.right
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.ratelog.*
import org.ratelog.movie.Movie
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.MovieFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvShow

class DailyJobToSyncMetadataTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var job: DailyJobToSyncMetadata

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        movieRepository = InMemoryMovieRepository()
        job = DailyJobToSyncMetadata(tmdbClient, tvShowRepository, movieRepository)
    }

    @Test
    fun `should sync active tv shows when job runs`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 100, name = "Test Show", status = Status.RETURNING_SERIES)
        tvShowRepository.save(show)

        val updatedShow = show.copy(overview = Overview("Updated overview"))
        whenever(tmdbClient.tvShowDetails(TmdbId(100))).thenReturn(updatedShow.right())

        job.sync()

        val saved = tvShowRepository.findById(TvShow.Id(1))
        assertNotNull(saved)
        assertEquals("Updated overview", saved?.overview?.value)
    }

    @Test
    fun `should sync active movies when job runs`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 200, title = "Test Movie", status = Status.POST_PRODUCTION)
        movieRepository.save(movie)

        val updatedMovie = movie.copy(overview = Overview("Updated movie overview"))
        whenever(tmdbClient.movieDetails(TmdbId(200))).thenReturn(updatedMovie.right())

        job.sync()

        val saved = movieRepository.findById(Movie.Id(1))
        assertNotNull(saved)
        assertEquals("Updated movie overview", saved?.overview?.value)
    }

    @Test
    fun `should not sync released or canceled tv shows`() {
        val endedShow = TvShowFactory.aTvShow(id = 1, tmdbId = 100, name = "Ended Show", status = Status.ENDED)
        val canceledShow = TvShowFactory.aTvShow(id = 2, tmdbId = 101, name = "Canceled Show", status = Status.CANCELED)
        val activeShow = TvShowFactory.aTvShow(id = 3, tmdbId = 102, name = "Active Show", status = Status.RETURNING_SERIES)
        tvShowRepository.save(endedShow)
        tvShowRepository.save(canceledShow)
        tvShowRepository.save(activeShow)

        val updatedActiveShow = activeShow.copy(overview = Overview("Updated"))
        whenever(tmdbClient.tvShowDetails(TmdbId(102))).thenReturn(updatedActiveShow.right())

        job.sync()

        verify(tmdbClient).tvShowDetails(TmdbId(102))
    }

    @Test
    fun `should not sync released or canceled movies`() {
        val releasedMovie = MovieFactory.aMovie(id = 1, tmdbId = 200, title = "Released", status = Status.RELEASED)
        val canceledMovie = MovieFactory.aMovie(id = 2, tmdbId = 201, title = "Canceled", status = Status.CANCELED)
        val activeMovie = MovieFactory.aMovie(id = 3, tmdbId = 202, title = "Active", status = Status.RUMORED)
        movieRepository.save(releasedMovie)
        movieRepository.save(canceledMovie)
        movieRepository.save(activeMovie)

        val updatedActiveMovie = activeMovie.copy(overview = Overview("Updated"))
        whenever(tmdbClient.movieDetails(TmdbId(202))).thenReturn(updatedActiveMovie.right())

        job.sync()

        verify(tmdbClient).movieDetails(TmdbId(202))
    }

    @Test
    fun `should log error and continue when tmdb fails for a tv show`() {
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 100, name = "Show 1", status = Status.RETURNING_SERIES)
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 101, name = "Show 2", status = Status.RETURNING_SERIES)
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)

        whenever(tmdbClient.tvShowDetails(TmdbId(100))).thenReturn(TmdbError.MovieNotFound.left())
        val updatedShow2 = show2.copy(overview = Overview("Updated"))
        whenever(tmdbClient.tvShowDetails(TmdbId(101))).thenReturn(updatedShow2.right())

        job.sync()

        val saved = tvShowRepository.findById(TvShow.Id(2))
        assertNotNull(saved)
        assertEquals("Updated", saved?.overview?.value)
    }

    @Test
    fun `should log error and continue when tmdb fails for a movie`() {
        val movie1 = MovieFactory.aMovie(id = 1, tmdbId = 200, title = "Movie 1", status = Status.IN_PRODUCTION)
        val movie2 = MovieFactory.aMovie(id = 2, tmdbId = 201, title = "Movie 2", status = Status.PLANNED)
        movieRepository.save(movie1)
        movieRepository.save(movie2)

        whenever(tmdbClient.movieDetails(TmdbId(200))).thenReturn(TmdbError.MovieNotFound.left())
        val updatedMovie2 = movie2.copy(overview = Overview("Updated"))
        whenever(tmdbClient.movieDetails(TmdbId(201))).thenReturn(updatedMovie2.right())

        job.sync()

        val saved = movieRepository.findById(Movie.Id(2))
        assertNotNull(saved)
        assertEquals("Updated", saved?.overview?.value)
    }
}
