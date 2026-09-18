package org.ratelog.config.istmdbconfigured

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.test.InMemoryGeneralConfigRepository

class IsTmdbConfiguredHandlerTest {

    private lateinit var repository: InMemoryGeneralConfigRepository
    private lateinit var handler: IsTmdbConfiguredHandler

    @BeforeEach
    fun setUp() {
        repository = InMemoryGeneralConfigRepository()
        handler = IsTmdbConfiguredHandler(repository)
    }

    @Test
    fun `given no stored TMDB API key when handling then returns false`() {
        // Given
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.REMEMBER_ME_KEY,
                value = "remember-value",
                updatedAtEpochMs = 1L,
            )
        )

        // When
        val result = handler.handle(IsTmdbConfiguredQuery)

        // Then
        assertTrue(result.isRight())
        assertEquals(false, result.fold({ Unit }, { it }))
    }

    @Test
    fun `given a stored TMDB API key when handling then returns true`() {
        // Given
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = "tmdb-key",
                updatedAtEpochMs = 1L,
            )
        )

        // When
        val result = handler.handle(IsTmdbConfiguredQuery)

        // Then
        assertTrue(result.isRight())
        assertEquals(true, result.fold({ Unit }, { it }))
    }

    @Test
    fun `given a blank stored TMDB API key when handling then returns false`() {
        // Given
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = "   ",
                updatedAtEpochMs = 1L,
            )
        )

        // When
        val result = handler.handle(IsTmdbConfiguredQuery)

        // Then
        assertTrue(result.isRight())
        assertEquals(false, result.fold({ Unit }, { it }))
    }
}