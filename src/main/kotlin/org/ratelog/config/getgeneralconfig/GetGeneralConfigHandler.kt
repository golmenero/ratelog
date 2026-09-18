package org.ratelog.config.getgeneralconfig

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.config.GeneralConfigRepository
import org.springframework.stereotype.Component

sealed interface GetGeneralConfigHandlerError {
    data object Forbidden : GetGeneralConfigHandlerError
}

data class GeneralConfigEntry(
    val key: ConfigKey,
    val value: String,
    val updatedAtEpochMs: Long,
)

@Component
class GetGeneralConfigHandler(
    private val generalConfigRepository: GeneralConfigRepository,
) {
    fun handle(query: GetGeneralConfigQuery): Either<GetGeneralConfigHandlerError, List<GeneralConfigEntry>> = either {
        ensure(query.currentUser.role.isAdminLike) { GetGeneralConfigHandlerError.Forbidden }
        generalConfigRepository.findAll().map { it.toEntry() }
    }

    private fun GeneralConfig.toEntry(): GeneralConfigEntry = GeneralConfigEntry(
        key = key,
        value = value,
        updatedAtEpochMs = updatedAtEpochMs,
    )
}
