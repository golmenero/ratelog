package org.ratelog.tmdb

import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.config.GeneralConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TmdbApiKeySeeder(
    private val generalConfigRepository: GeneralConfigRepository,
    @Value("\${TMDB_API_KEY:}") private val envApiKey: String,
) {
    private val logger = LoggerFactory.getLogger(TmdbApiKeySeeder::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun seed() {
        if (envApiKey.isBlank()) return
        if (generalConfigRepository.findByKey(ConfigKey.TMDB_API_KEY) != null) return

        generalConfigRepository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = envApiKey,
                updatedAtEpochMs = System.currentTimeMillis(),
            )
        )
        logger.info("TMDB API key seeded from TMDB_API_KEY env var into general_config")
    }
}
