package org.ratelog.config

data class GeneralConfig(
    val key: ConfigKey,
    val value: String,
    val updatedAtEpochMs: Long,
)
