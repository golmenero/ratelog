package org.ratelog.config

interface GeneralConfigRepository {
    fun findByKey(key: ConfigKey): GeneralConfig?
    fun findAll(): List<GeneralConfig>
    fun save(config: GeneralConfig)
}
