package org.ratelog.movie.description

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.*
import org.ratelog.movie.MovieDescription
import org.ratelog.test.InMemoryMovieDescriptionRepository

class MovieDescriptionRepositoryTest {

    private lateinit var repository: InMemoryMovieDescriptionRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryMovieDescriptionRepository()
    }

    @Test
    fun `should return null when no description exists for tmdbId and lang`() {
        val result = repository.findByTmdbIdAndLang(TmdbId(1), Lang.en)

        assertNull(result)
    }

    @Test
    fun `should return description when it exists for tmdbId and lang`() {
        val desc = MovieDescription(null,TmdbId(1), Lang.en, Title("Test"), Overview("Overview"))
        repository.saveAll(listOf(desc))

        val result = repository.findByTmdbIdAndLang(TmdbId(1), Lang.en)

        assertNotNull(result)
        assertEquals("Test", result?.title?.value)
        assertEquals("Overview", result?.overview?.value)
    }

    @Test
    fun `should return all descriptions for a tmdbId`() {
        val descs = listOf(
            MovieDescription(null,TmdbId(1), Lang.en, Title("EN Title"), Overview("EN Overview")),
            MovieDescription(null,TmdbId(1), Lang.es, Title("ES Title"), Overview("ES Overview")),
            MovieDescription(null,TmdbId(2), Lang.en, Title("Other"), null),
        )
        repository.saveAll(descs)

        val result = repository.findAllByTmdbId(TmdbId(1))

        assertEquals(2, result.size)
        assertTrue(result.all { it.tmdbId == TmdbId(1) })
    }

    @Test
    fun `should return false when no descriptions exist for tmdbId`() {
        val exists = repository.existsAnyByTmdbId(TmdbId(1))

        assertFalse(exists)
    }

    @Test
    fun `should return true when at least one description exists for tmdbId`() {
        repository.saveAll(listOf(MovieDescription(null,TmdbId(1), Lang.en, Title("Test"), null)))

        val exists = repository.existsAnyByTmdbId(TmdbId(1))

        assertTrue(exists)
    }
}
