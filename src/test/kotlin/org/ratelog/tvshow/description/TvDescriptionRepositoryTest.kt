package org.ratelog.tvshow.description

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.*
import org.ratelog.test.InMemoryTvDescriptionRepository
import org.ratelog.tvshow.TvDescription

class TvDescriptionRepositoryTest {

    private lateinit var repository: InMemoryTvDescriptionRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryTvDescriptionRepository()
    }

    @Test
    fun `should return null when no description exists for tmdbId and lang`() {
        val result = repository.findByTmdbIdAndLang(TmdbId(1), Lang.en)

        assertNull(result)
    }

    @Test
    fun `should return description when it exists for tmdbId and lang`() {
        val desc = TvDescription(TmdbId(1), Lang.en, Title("Test"), Overview("Overview"))
        repository.saveAll(listOf(desc))

        val result = repository.findByTmdbIdAndLang(TmdbId(1), Lang.en)

        assertNotNull(result)
        assertEquals("Test", result?.name?.value)
        assertEquals("Overview", result?.overview?.value)
    }

    @Test
    fun `should return all descriptions for a tmdbId`() {
        val descs = listOf(
            TvDescription(TmdbId(1), Lang.en, Title("EN Name"), Overview("EN Overview")),
            TvDescription(TmdbId(1), Lang.es, Title("ES Name"), Overview("ES Overview")),
            TvDescription(TmdbId(2), Lang.en, Title("Other"), null),
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
        repository.saveAll(listOf(TvDescription(TmdbId(1), Lang.en, Title("Test"), null)))

        val exists = repository.existsAnyByTmdbId(TmdbId(1))

        assertTrue(exists)
    }

    @Test
    fun `should overwrite existing description when saving with same tmdbId and lang`() {
        val original = TvDescription(TmdbId(1), Lang.en, Title("Original"), Overview("Original"))
        repository.saveAll(listOf(original))

        val updated = TvDescription(TmdbId(1), Lang.en, Title("Updated"), Overview("Updated"))
        repository.saveAll(listOf(updated))

        val result = repository.findByTmdbIdAndLang(TmdbId(1), Lang.en)
        assertEquals("Updated", result?.name?.value)
    }
}
