package org.ratelog.config

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.ParseError

enum class ConfigKey(val value: String) {
    REMEMBER_ME_KEY("remember_me_key"),
    TMDB_API_KEY("tmdb_api_key");

    companion object {
        fun parse(value: String): Either<ParseError, ConfigKey> = either {
            entries.firstOrNull { it.value == value }
                ?: raise(ParseError.InvalidConfigKey)
        }
    }
}
