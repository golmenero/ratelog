package org.ratelog.config.getgeneralconfig

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.test.InMemoryGeneralConfigRepository
import org.ratelog.test.UserFactory

class GetGeneralConfigHandlerTest {

    private lateinit var repository: InMemoryGeneralConfigRepository
    private lateinit var handler: GetGeneralConfigHandler

    @BeforeEach
    fun setUp() {
        repository = InMemoryGeneralConfigRepository()
        handler = GetGeneralConfigHandler(repository)
    }

    @Test
    fun `given a USER current user when querying then returns Forbidden`() {
        // Given
        val user = UserFactory.aUser()

        // When
        val result = handler.handle(GetGeneralConfigQuery(user))

        // Then
        assertTrue(result.isLeft())
        assertEquals(GetGeneralConfigHandlerError.Forbidden, result.fold({ it }, { Unit }))
    }

    @Test
    fun `given an ADMIN current user with no configs when querying then returns empty list`() {
        // Given
        val admin = UserFactory.aUser(role = org.ratelog.Role.ADMIN)

        // When
        val result = handler.handle(GetGeneralConfigQuery(admin))

        // Then
        assertTrue(result.isRight())
        assertEquals(emptyList<GeneralConfigEntry>(), result.fold({ emptyList() }, { it }))
    }

    @Test
    fun `given an ADMIN current user with configs when querying then returns all sorted by key`() {
        // Given
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = "tmdb-value",
                updatedAtEpochMs = 1L,
            )
        )
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.REMEMBER_ME_KEY,
                value = "remember-value",
                updatedAtEpochMs = 2L,
            )
        )
        val admin = UserFactory.aUser(role = org.ratelog.Role.ADMIN)

        // When
        val result = handler.handle(GetGeneralConfigQuery(admin))

        // Then
        assertTrue(result.isRight())
        val entries = result.fold({ emptyList() }, { it })
        assertEquals(2, entries.size)
        assertEquals("remember_me_key", entries[0].key.value)
        assertEquals("tmdb_api_key", entries[1].key.value)
    }

    @Test
    fun `given a SUPERADMIN current user when querying then succeeds regardless of stored configs`() {
        // Given
        val superadmin = UserFactory.aUser(role = org.ratelog.Role.SUPERADMIN)

        // When
        val result = handler.handle(GetGeneralConfigQuery(superadmin))

        // Then
        assertTrue(result.isRight())
    }
}
