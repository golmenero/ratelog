package org.ratelog.system

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.test.InMemoryGeneralConfigRepository

class RememberMeKeyProviderTest {

    @Test
    fun `should generate and persist a new key when none exists in repository`() {
        val repository = InMemoryGeneralConfigRepository()
        val provider = RememberMeKeyProvider(repository)

        provider.init()

        val persisted = repository.findByKey(ConfigKey.REMEMBER_ME_KEY)
        assertNotNull(persisted)
        assertEquals(provider.key(), persisted!!.value)
    }

    @Test
    fun `should generate a 64 character hex key on first initialization`() {
        val repository = InMemoryGeneralConfigRepository()
        val provider = RememberMeKeyProvider(repository)

        provider.init()

        val value = provider.key()
        assertEquals(64, value.length)
        assertTrue(value.matches(Regex("^[0-9a-f]+$")), "expected hex chars but was: $value")
    }

    @Test
    fun `should reuse existing key when repository already has one`() {
        val repository = InMemoryGeneralConfigRepository()
        val existingKey = "a".repeat(64)
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.REMEMBER_ME_KEY,
                value = existingKey,
                updatedAtEpochMs = 0L,
            )
        )
        val provider = RememberMeKeyProvider(repository)

        provider.init()

        assertEquals(existingKey, provider.key())
        assertEquals(0L, repository.findByKey(ConfigKey.REMEMBER_ME_KEY)!!.updatedAtEpochMs)
    }

    @Test
    fun `should return the same key on every call`() {
        val repository = InMemoryGeneralConfigRepository()
        val provider = RememberMeKeyProvider(repository)
        provider.init()

        val first = provider.key()
        val second = provider.key()
        val third = provider.key()

        assertEquals(first, second)
        assertEquals(second, third)
    }

    @Test
    fun `should generate different keys across provider instances when repo is empty`() {
        val firstRepo = InMemoryGeneralConfigRepository()
        val secondRepo = InMemoryGeneralConfigRepository()
        val firstProvider = RememberMeKeyProvider(firstRepo)
        val secondProvider = RememberMeKeyProvider(secondRepo)

        firstProvider.init()
        secondProvider.init()

        assertNotEquals(firstProvider.key(), secondProvider.key())
    }

    @Test
    fun `should reuse the same persisted key across provider instances`() {
        val repository = InMemoryGeneralConfigRepository()
        val firstProvider = RememberMeKeyProvider(repository)
        firstProvider.init()
        val originalKey = firstProvider.key()

        val secondProvider = RememberMeKeyProvider(repository)
        secondProvider.init()

        assertEquals(originalKey, secondProvider.key())
    }
}
