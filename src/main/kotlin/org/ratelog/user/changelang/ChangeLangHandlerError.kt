package org.ratelog.user.changelang

sealed interface ChangeLangHandlerError {
    data object UserNotFound : ChangeLangHandlerError
}
