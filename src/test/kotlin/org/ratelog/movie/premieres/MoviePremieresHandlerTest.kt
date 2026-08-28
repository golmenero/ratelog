package org.ratelog.movie.premieres

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.*
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieDescription
import org.ratelog.test.InMemoryMovieDescriptionRepository
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.MovieFactory
import org.ratelog.user.User
import java.time.LocalDate

class MoviePremieresHandlerTest {
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var movieDescriptionRepository: InMemoryMovieDescriptionRepository
    private lateinit var handler: MoviePremieresHandler

    @BeforeEach
    fun setUp() {
        movieRepository = InMemoryMovieRepository()
        movieDescriptionRepository = InMemoryMovieDescriptionRepository()
        handler = MoviePremieresHandler(movieRepository, movieDescriptionRepository)
    }

    @Test
    fun `should return empty premieres when no followed movies`() {
        // given
        val query = MoviePremieresQuery(User.Id(1), Lang.en)

        // when
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertTrue(premieres.items.isEmpty())
            }
        )
    }

    @Test
    fun `should return all followed movies in a single list`() {
        // given
        val movie1 = MovieFactory.aMovie(id = 1, tmdbId = 123, originalTitle = "Released Movie", releaseDate = LocalDate.now().minusDays(1))
        val movie2 = MovieFactory.aMovie(id = 2, tmdbId = 456, originalTitle = "Upcoming Movie", releaseDate = LocalDate.now().plusDays(30))
        val movie3 = MovieFactory.aMovie(id = 3, tmdbId = 789, originalTitle = "No Date Movie", releaseDate = null)
        movieRepository.save(movie1)
        movieRepository.save(movie2)
        movieRepository.save(movie3)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null, TmdbId(123), Lang.en, Title("Released Movie"), null),
            MovieDescription(null, TmdbId(456), Lang.en, Title("Upcoming Movie"), null),
            MovieDescription(null, TmdbId(789), Lang.en, Title("No Date Movie"), null),
        ))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(1))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(2))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(3))

        // when
        val query = MoviePremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(3, premieres.items.size)
            }
        )
    }

    @Test
    fun `should sort released first then upcoming then no date`() {
        // given
        val noDateMovie = MovieFactory.aMovie(id = 1, tmdbId = 123, originalTitle = "No Date Movie", releaseDate = null)
        val upcomingMovie = MovieFactory.aMovie(id = 2, tmdbId = 456, originalTitle = "Upcoming Movie", releaseDate = LocalDate.now().plusDays(30))
        val releasedMovie = MovieFactory.aMovie(id = 3, tmdbId = 789, originalTitle = "Released Movie", releaseDate = LocalDate.now().minusDays(1))
        movieRepository.save(noDateMovie)
        movieRepository.save(upcomingMovie)
        movieRepository.save(releasedMovie)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null, TmdbId(123), Lang.en, Title("No Date Movie"), null),
            MovieDescription(null, TmdbId(456), Lang.en, Title("Upcoming Movie"), null),
            MovieDescription(null, TmdbId(789), Lang.en, Title("Released Movie"), null),
        ))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(1))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(2))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(3))

        // when
        val query = MoviePremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals("Released Movie", premieres.items[0].title)
                assertTrue(premieres.items[0].isReleased)
                assertEquals("Upcoming Movie", premieres.items[1].title)
                assertFalse(premieres.items[1].isReleased)
                assertTrue(premieres.items[1].hasDate)
                assertEquals("No Date Movie", premieres.items[2].title)
                assertFalse(premieres.items[2].hasDate)
            }
        )
    }

    @Test
    fun `should mark movies with no date correctly`() {
        // given
        val movie = MovieFactory.aMovie(id = 1, tmdbId = 123, originalTitle = "No Date Movie", releaseDate = null)
        movieRepository.save(movie)
        movieDescriptionRepository.saveAll(listOf(
            MovieDescription(null, TmdbId(123), Lang.en, Title("No Date Movie"), null),
        ))
        movieRepository.toggleFollow(User.Id(1), Movie.Id(1))

        // when
        val query = MoviePremieresQuery(User.Id(1), Lang.en)
        val result = handler.handle(query)

        // then
        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { premieres ->
                assertEquals(1, premieres.items.size)
                assertEquals("No Date Movie", premieres.items[0].title)
                assertFalse(premieres.items[0].hasDate)
            }
        )
    }
}
