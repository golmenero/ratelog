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
import org.ratelog.movie.MovieDescription
import org.ratelog.test.InMemoryMovieDescriptionRepository
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.InMemoryTvDescriptionRepository
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.test.MovieFactory
import org.ratelog.test.TvShowFactory
import org.ratelog.tvshow.TvDescription
import org.ratelog.tvshow.TvShow

class DailyJobToSyncMetadataTest {

    private val tmdbClient: TmdbClient = mock()
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var tvDescriptionRepository: InMemoryTvDescriptionRepository
    private lateinit var movieDescriptionRepository: InMemoryMovieDescriptionRepository
    private lateinit var job: DailyJobToSyncMetadata

    @BeforeEach
    fun setUp() {
        tvShowRepository = InMemoryTvShowRepository()
        movieRepository = InMemoryMovieRepository()
        tvDescriptionRepository = InMemoryTvDescriptionRepository()
        movieDescriptionRepository = InMemoryMovieDescriptionRepository()
        job = DailyJobToSyncMetadata(tmdbClient, tvShowRepository, movieRepository, tvDescriptionRepository, movieDescriptionRepository)
    }

    @Test
    fun `should sync active tv shows when job runs`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 100, originalName = "Test Show", status = Status.RETURNING_SERIES)
        tvShowRepository.save(show)

        val updatedShow = show.copy(originalName = Title("Updated Name"))
        whenever(tmdbClient.tvShowDetails(TmdbId(100))).thenReturn(updatedShow.right())
        whenever(tmdbClient.tvTranslations(TmdbId(100), Title("a-title"))).thenReturn(emptyList<TvDescription>().right())

        job.sync()

        val saved = tvShowRepository.findById(TvShow.Id(1))
        assertNotNull(saved)
        assertEquals("Updated Name", saved?.originalName?.value)
    }

    @Test
    fun `should sync active movies when job runs`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 200, originalTitle = "Test Movie", status = Status.POST_PRODUCTION)
        movieRepository.save(movie)

        val updatedMovie = movie.copy(originalTitle = Title("Updated Title"))
        whenever(tmdbClient.movieDetails(TmdbId(200))).thenReturn(updatedMovie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(200), Title("a-title"))).thenReturn(emptyList<MovieDescription>().right())

        job.sync()

        val saved = movieRepository.findById(Movie.Id(1))
        assertNotNull(saved)
        assertEquals("Updated Title", saved?.originalTitle?.value)
    }

    @Test
    fun `should not sync released or canceled tv shows`() {
        val endedShow = TvShowFactory.aTvShow(id = 1, tmdbId = 100, originalName = "Ended Show", status = Status.ENDED)
        val canceledShow = TvShowFactory.aTvShow(id = 2, tmdbId = 101, originalName = "Canceled Show", status = Status.CANCELED)
        val activeShow = TvShowFactory.aTvShow(id = 3, tmdbId = 102, originalName = "Active Show", status = Status.RETURNING_SERIES)
        tvShowRepository.save(endedShow)
        tvShowRepository.save(canceledShow)
        tvShowRepository.save(activeShow)

        val updatedActiveShow = activeShow.copy(originalName = Title("Updated"))
        whenever(tmdbClient.tvShowDetails(TmdbId(102))).thenReturn(updatedActiveShow.right())
        whenever(tmdbClient.tvTranslations(TmdbId(102), Title("a-title"))).thenReturn(emptyList<TvDescription>().right())

        job.sync()

        verify(tmdbClient).tvShowDetails(TmdbId(102))
    }

    @Test
    fun `should not sync released or canceled movies`() {
        val releasedMovie = MovieFactory.aMovie(id = 1, tmdbId = 200, originalTitle = "Released", status = Status.RELEASED)
        val canceledMovie = MovieFactory.aMovie(id = 2, tmdbId = 201, originalTitle = "Canceled", status = Status.CANCELED)
        val activeMovie = MovieFactory.aMovie(id = 3, tmdbId = 202, originalTitle = "Active", status = Status.RUMORED)
        movieRepository.save(releasedMovie)
        movieRepository.save(canceledMovie)
        movieRepository.save(activeMovie)

        val updatedActiveMovie = activeMovie.copy(originalTitle = Title("Updated"))
        whenever(tmdbClient.movieDetails(TmdbId(202))).thenReturn(updatedActiveMovie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(202), Title("a-title"))).thenReturn(emptyList<MovieDescription>().right())

        job.sync()

        verify(tmdbClient).movieDetails(TmdbId(202))
    }

    @Test
    fun `should log error and continue when tmdb fails for a tv show`() {
        val show1 = TvShowFactory.aTvShow(id = 1, tmdbId = 100, originalName = "Show 1", status = Status.RETURNING_SERIES)
        val show2 = TvShowFactory.aTvShow(id = 2, tmdbId = 101, originalName = "Show 2", status = Status.RETURNING_SERIES)
        tvShowRepository.save(show1)
        tvShowRepository.save(show2)

        whenever(tmdbClient.tvShowDetails(TmdbId(100))).thenReturn(TmdbError.MovieNotFound.left())
        val updatedShow2 = show2.copy(originalName = Title("Updated"))
        whenever(tmdbClient.tvShowDetails(TmdbId(101))).thenReturn(updatedShow2.right())
        whenever(tmdbClient.tvTranslations(TmdbId(101), Title("a-title"))).thenReturn(emptyList<TvDescription>().right())

        job.sync()

        val saved = tvShowRepository.findById(TvShow.Id(2))
        assertNotNull(saved)
        assertEquals("Updated", saved?.originalName?.value)
    }

    @Test
    fun `should log error and continue when tmdb fails for a movie`() {
        val movie1 = MovieFactory.aMovie(id = 1, tmdbId = 200, originalTitle = "Movie 1", status = Status.IN_PRODUCTION)
        val movie2 = MovieFactory.aMovie(id = 2, tmdbId = 201, originalTitle = "Movie 2", status = Status.PLANNED)
        movieRepository.save(movie1)
        movieRepository.save(movie2)

        whenever(tmdbClient.movieDetails(TmdbId(200))).thenReturn(TmdbError.MovieNotFound.left())
        val updatedMovie2 = movie2.copy(originalTitle = Title("Updated"))
        whenever(tmdbClient.movieDetails(TmdbId(201))).thenReturn(updatedMovie2.right())
        whenever(tmdbClient.movieTranslations(TmdbId(201), Title("a-title"))).thenReturn(emptyList<MovieDescription>().right())

        job.sync()

        val saved = movieRepository.findById(Movie.Id(2))
        assertNotNull(saved)
        assertEquals("Updated", saved?.originalTitle?.value)
    }

    @Test
    fun `should fetch translations when no descriptions exist for movie`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 200, originalTitle = "Test", status = Status.RUMORED)
        movieRepository.save(movie)

        val translations = listOf(
            MovieDescription(null,TmdbId(200), Lang.en, Title("EN Title"), Overview("EN Overview")),
            MovieDescription(null,TmdbId(200), Lang.es, Title("ES Title"), Overview("ES Overview")),
        )
        whenever(tmdbClient.movieDetails(TmdbId(200))).thenReturn(movie.right())
        whenever(tmdbClient.movieTranslations(TmdbId(200), Title("Test"))).thenReturn(translations.right())

        job.sync()

        val saved = movieDescriptionRepository.findAllByTmdbId(TmdbId(200))
        assertEquals(2, saved.size)
    }

    @Test
    fun `should not fetch translations when descriptions already exist for movie`() {
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 200, originalTitle = "Test", status = Status.RUMORED)
        movieRepository.save(movie)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null,TmdbId(200), Lang.en, Title("EN Title"), Overview("EN Overview"))
        ))

        whenever(tmdbClient.movieDetails(TmdbId(200))).thenReturn(movie.right())

        job.sync()

        verify(tmdbClient, org.mockito.kotlin.never()).movieTranslations(TmdbId(200), Title("a-title"))
    }

    @Test
    fun `should fetch translations when no descriptions exist for tv show`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 100, originalName = "Test", status = Status.RETURNING_SERIES)
        tvShowRepository.save(show)

        val translations = listOf(
            TvDescription(null,TmdbId(100), Lang.en, Title("EN Name"), Overview("EN Overview")),
            TvDescription(null,TmdbId(100), Lang.es, Title("ES Name"), Overview("ES Overview")),
        )
        whenever(tmdbClient.tvShowDetails(TmdbId(100))).thenReturn(show.right())
        whenever(tmdbClient.tvTranslations(TmdbId(100), Title("Test"))).thenReturn(translations.right())

        job.sync()

        val saved = tvDescriptionRepository.findAllByTmdbId(TmdbId(100))
        assertEquals(2, saved.size)
    }

    @Test
    fun `should not fetch translations when descriptions already exist for tv show`() {
        val show = TvShowFactory.aTvShow(id = 1, tmdbId = 100, originalName = "Test", status = Status.RETURNING_SERIES)
        tvShowRepository.save(show)
        tvDescriptionRepository.saveAll(listOf(
            TvDescription(null,TmdbId(100), Lang.en, Title("EN Name"), Overview("EN Overview"))
        ))

        whenever(tmdbClient.tvShowDetails(TmdbId(100))).thenReturn(show.right())

        job.sync()

        verify(tmdbClient, org.mockito.kotlin.never()).tvTranslations(TmdbId(100), Title("a-title"))
    }
}
