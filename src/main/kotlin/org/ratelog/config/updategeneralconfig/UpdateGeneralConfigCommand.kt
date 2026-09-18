package org.ratelog.config.updategeneralconfig

import org.ratelog.config.ConfigKey

data class UpdateGeneralConfigCommand(
    val key: ConfigKey,
    val value: String,
)
