package org.ratelog.tmdb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.test.InMemoryGeneralConfigRepository

class TmdbApiKeySeederTest {

    @Test
    fun `given an empty env var when seeding then no value is persisted`() {
        // Given
        val repository = InMemoryGeneralConfigRepository()
        val seeder = TmdbApiKeySeeder(repository, envApiKey = "")

        // When
        seeder.seed()

        // Then
        assertNull(repository.findByKey(ConfigKey.TMDB_API_KEY))
    }

    @Test
    fun `given a blank env var when seeding then no value is persisted`() {
        // Given
        val repository = InMemoryGeneralConfigRepository()
        val seeder = TmdbApiKeySeeder(repository, envApiKey = "   ")

        // When
        seeder.seed()

        // Then
        assertNull(repository.findByKey(ConfigKey.TMDB_API_KEY))
    }

    @Test
    fun `given an env var and no existing config when seeding then env value is persisted`() {
        // Given
        val repository = InMemoryGeneralConfigRepository()
        val seeder = TmdbApiKeySeeder(repository, envApiKey = "seeded-from-env")

        // When
        seeder.seed()

        // Then
        val stored = repository.findByKey(ConfigKey.TMDB_API_KEY)
        assertNotNull(stored)
        assertEquals("seeded-from-env", stored!!.value)
        assertTrue(stored.updatedAtEpochMs > 0L)
    }

    @Test
    fun `given an env var and an existing config when seeding then existing value is preserved`() {
        // Given
        val repository = InMemoryGeneralConfigRepository()
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = "already-saved",
                updatedAtEpochMs = 0L,
            )
        )
        val seeder = TmdbApiKeySeeder(repository, envApiKey = "seeded-from-env")

        // When
        seeder.seed()

        // Then
        val stored = repository.findByKey(ConfigKey.TMDB_API_KEY)!!
        assertEquals("already-saved", stored.value)
        assertEquals(0L, stored.updatedAtEpochMs)
    }
}
