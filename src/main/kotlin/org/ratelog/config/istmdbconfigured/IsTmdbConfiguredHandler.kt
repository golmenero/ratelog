package org.ratelog.config.istmdbconfigured

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfigRepository
import org.springframework.stereotype.Component

@Component
class IsTmdbConfiguredHandler(
    private val generalConfigRepository: GeneralConfigRepository,
) {
    fun handle(query: IsTmdbConfiguredQuery): Either<Nothing, Boolean> = either {
        generalConfigRepository.findByKey(ConfigKey.TMDB_API_KEY)
            ?.value
            ?.isNotBlank()
            ?: false
    }
}