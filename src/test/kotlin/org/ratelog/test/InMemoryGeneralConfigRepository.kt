package org.ratelog.test

import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.config.GeneralConfigRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryGeneralConfigRepository : GeneralConfigRepository {
    private val store = ConcurrentHashMap<String, GeneralConfig>()

    override fun findByKey(key: ConfigKey): GeneralConfig? = store[key.value]

    override fun findAll(): List<GeneralConfig> = store.values.sortedBy { it.key.value }

    override fun save(config: GeneralConfig) {
        store[config.key.value] = config
    }
}
