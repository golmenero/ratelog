package org.ratelog.config

import arrow.core.Either
import arrow.core.raise.either

data class ConfigKey(val value: String) {
    companion object {
        private val regex = Regex("^[a-z][a-z0-9_]{2,49}$")

        fun parse(value: String): Either<ParseError, ConfigKey> = either {
            if (!value.matches(regex)) raise(ParseError.InvalidConfigKey)
            ConfigKey(value)
        }

        fun unsafe(value: String): ConfigKey = ConfigKey(value)
    }
}

sealed interface ParseError {
    data object InvalidConfigKey : ParseError
}
