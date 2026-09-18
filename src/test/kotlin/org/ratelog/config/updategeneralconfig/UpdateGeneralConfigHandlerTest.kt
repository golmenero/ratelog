package org.ratelog.config.updategeneralconfig

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.test.InMemoryGeneralConfigRepository

class UpdateGeneralConfigHandlerTest {

    private lateinit var repository: InMemoryGeneralConfigRepository
    private lateinit var handler: UpdateGeneralConfigHandler

    @BeforeEach
    fun setUp() {
        repository = InMemoryGeneralConfigRepository()
        handler = UpdateGeneralConfigHandler(repository)
    }

    @Test
    fun `given a non-existent key when updating then it is persisted`() {
        // Given
        val command = UpdateGeneralConfigCommand(
            key = ConfigKey.unsafe("tmdb_api_key"),
            value = "abc123",
        )

        // When
        val result = handler.handle(command)

        // Then
        assertTrue(result.isRight())
        val saved = repository.findByKey(command.key)
        assertNotNull(saved)
        assertEquals("abc123", saved!!.value)
    }

    @Test
    fun `given an existing key when updating then value is overwritten and id is preserved`() {
        // Given
        val key = ConfigKey.unsafe("tmdb_api_key")
        repository.save(
            GeneralConfig(
                id = null,
                key = key,
                value = "old",
                updatedAtEpochMs = 0L,
            )
        )
        val command = UpdateGeneralConfigCommand(key = key, value = "new")

        // When
        val result = handler.handle(command)

        // Then
        assertTrue(result.isRight())
        val saved = repository.findByKey(key)!!
        assertEquals("new", saved.value)
        assertTrue(saved.updatedAtEpochMs > 0L)
    }

    @Test
    fun `given an empty value when updating then returns EmptyValue and does not persist`() {
        // Given
        val key = ConfigKey.unsafe("tmdb_api_key")
        repository.save(
            GeneralConfig(
                id = null,
                key = key,
                value = "old",
                updatedAtEpochMs = 0L,
            )
        )
        val command = UpdateGeneralConfigCommand(key = key, value = "   ")

        // When
        val result = handler.handle(command)

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateGeneralConfigHandlerError.EmptyValue, result.fold({ it }, { Unit }))
        val saved = repository.findByKey(key)!!
        assertEquals("old", saved.value)
    }

    @Test
    fun `given a value longer than 500 chars when updating then returns ValueTooLong`() {
        // Given
        val command = UpdateGeneralConfigCommand(
            key = ConfigKey.unsafe("tmdb_api_key"),
            value = "a".repeat(UpdateGeneralConfigHandler.MAX_VALUE_LENGTH + 1),
        )

        // When
        val result = handler.handle(command)

        // Then
        assertTrue(result.isLeft())
        assertEquals(UpdateGeneralConfigHandlerError.ValueTooLong, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given a value exactly at the max length when updating then it is persisted`() {
        // Given
        val command = UpdateGeneralConfigCommand(
            key = ConfigKey.unsafe("tmdb_api_key"),
            value = "a".repeat(UpdateGeneralConfigHandler.MAX_VALUE_LENGTH),
        )

        // When
        val result = handler.handle(command)

        // Then
        assertTrue(result.isRight())
        val saved = repository.findByKey(command.key)!!
        assertEquals(UpdateGeneralConfigHandler.MAX_VALUE_LENGTH, saved.value.length)
    }
}
