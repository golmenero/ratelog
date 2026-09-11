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

        val persisted = repository.findByKey(ConfigKey.unsafe("remember_me_key"))
        assertNotNull(persisted)
        assertEquals(provider.key().value, persisted!!.value)
    }

    @Test
    fun `should generate a 64 character hex key on first initialization`() {
        val repository = InMemoryGeneralConfigRepository()
        val provider = RememberMeKeyProvider(repository)

        provider.init()

        val value = provider.key().value
        assertEquals(64, value.length)
        assertTrue(value.matches(Regex("^[0-9a-f]+$")), "expected hex chars but was: $value")
    }

    @Test
    fun `should reuse existing key when repository already has one`() {
        val repository = InMemoryGeneralConfigRepository()
        val existingKey = "a".repeat(64)
        repository.save(
            GeneralConfig(
                key = ConfigKey.unsafe("remember_me_key"),
                value = existingKey,
                updatedAtEpochMs = 0L,
            )
        )
        val provider = RememberMeKeyProvider(repository)

        provider.init()

        assertEquals(existingKey, provider.key().value)
        assertEquals(0L, repository.findByKey(ConfigKey.unsafe("remember_me_key"))!!.updatedAtEpochMs)
    }

    @Test
    fun `should return the same key on every call`() {
        val repository = InMemoryGeneralConfigRepository()
        val provider = RememberMeKeyProvider(repository)
        provider.init()

        val first = provider.key()
        val second = provider.key()
        val third = provider.key()

        assertEquals(first.value, second.value)
        assertEquals(second.value, third.value)
    }

    @Test
    fun `should generate different keys across provider instances when repo is empty`() {
        val firstRepo = InMemoryGeneralConfigRepository()
        val secondRepo = InMemoryGeneralConfigRepository()
        val firstProvider = RememberMeKeyProvider(firstRepo)
        val secondProvider = RememberMeKeyProvider(secondRepo)

        firstProvider.init()
        secondProvider.init()

        assertNotEquals(firstProvider.key().value, secondProvider.key().value)
    }

    @Test
    fun `should reuse the same persisted key across provider instances`() {
        val repository = InMemoryGeneralConfigRepository()
        val firstProvider = RememberMeKeyProvider(repository)
        firstProvider.init()
        val originalKey = firstProvider.key().value

        val secondProvider = RememberMeKeyProvider(repository)
        secondProvider.init()

        assertEquals(originalKey, secondProvider.key().value)
    }
}
