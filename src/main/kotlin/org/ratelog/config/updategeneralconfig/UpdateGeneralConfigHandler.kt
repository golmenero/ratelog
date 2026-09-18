package org.ratelog.config.updategeneralconfig

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.config.GeneralConfig
import org.ratelog.config.GeneralConfigRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateGeneralConfigHandler(
    private val generalConfigRepository: GeneralConfigRepository,
) {
    @Transactional
    fun handle(command: UpdateGeneralConfigCommand): Either<UpdateGeneralConfigHandlerError, GeneralConfig> = either {
        ensure(command.key.allowsBlankValue() || command.value.isNotBlank()) { UpdateGeneralConfigHandlerError.EmptyValue }
        ensure(command.value.length <= MAX_VALUE_LENGTH) { UpdateGeneralConfigHandlerError.ValueTooLong }

        val existing = generalConfigRepository.findByKey(command.key)
        val updated = GeneralConfig(
            id = existing?.id,
            key = command.key,
            value = command.value,
            updatedAtEpochMs = System.currentTimeMillis(),
        )
        generalConfigRepository.save(updated)
        updated
    }

    companion object {
        const val MAX_VALUE_LENGTH = 500
    }
}
