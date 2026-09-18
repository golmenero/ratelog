package org.ratelog.config.updategeneralconfig

sealed interface UpdateGeneralConfigHandlerError {
    data object EmptyValue : UpdateGeneralConfigHandlerError
    data object ValueTooLong : UpdateGeneralConfigHandlerError
}
