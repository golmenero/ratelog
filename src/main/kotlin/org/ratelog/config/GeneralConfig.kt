package org.ratelog.config

data class GeneralConfig(
    val id: Id?,
    val key: ConfigKey,
    val value: String,
    val updatedAtEpochMs: Long,
) {
    data class Id(val value: Long)
}
