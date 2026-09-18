package org.ratelog.config

import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class GeneralConfigRepositoryImpl(
    private val generalConfigDAO: GeneralConfigDAO,
) : GeneralConfigRepository {

    override fun findByKey(key: ConfigKey): GeneralConfig? =
        generalConfigDAO.findByKey(key.value).getOrNull()?.let(::toDomain)

    override fun findAll(): List<GeneralConfig> =
        generalConfigDAO.findAllOrdered().map(::toDomain)

    override fun save(config: GeneralConfig) {
        generalConfigDAO.save(toEntity(config))
    }

    private fun toDomain(entity: GeneralConfigEntity): GeneralConfig = GeneralConfig(
        id = entity.id?.let(GeneralConfig::Id),
        key = ConfigKey.unsafe(entity.key),
        value = entity.value,
        updatedAtEpochMs = entity.updatedAtEpochMs,
    )

    private fun toEntity(config: GeneralConfig): GeneralConfigEntity = GeneralConfigEntity(
        id = config.id?.value,
        key = config.key.value,
        value = config.value,
        updatedAtEpochMs = config.updatedAtEpochMs,
    )
}
