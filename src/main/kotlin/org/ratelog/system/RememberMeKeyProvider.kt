package org.ratelog.system

import jakarta.annotation.PostConstruct
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.config.GeneralConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class RememberMeKeyProvider(
    private val generalConfigRepository: GeneralConfigRepository,
) {
    private val logger = LoggerFactory.getLogger(RememberMeKeyProvider::class.java)
    private lateinit var key: ConfigKey

    @PostConstruct
    fun init() {
        key = generalConfigRepository.findByKey(REMEMBER_ME_KEY)?.value?.let(::ConfigKey)
            ?: generateAndPersist().also { logger.info("Remember-me key generated and persisted") }
    }

    fun key(): ConfigKey = key

    private fun generateAndPersist(): ConfigKey {
        val raw = ByteArray(KEY_BYTES).also(SecureRandom()::nextBytes)
        val generated = ConfigKey.unsafe(raw.toHex())
        generalConfigRepository.save(
            GeneralConfig(
                id = null,
                key = REMEMBER_ME_KEY,
                value = generated.value,
                updatedAtEpochMs = System.currentTimeMillis(),
            )
        )
        return generated
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { "%02x".format(it) }

    companion object {
        private val REMEMBER_ME_KEY = ConfigKey.unsafe("remember_me_key")
        private const val KEY_BYTES = 32
    }
}
