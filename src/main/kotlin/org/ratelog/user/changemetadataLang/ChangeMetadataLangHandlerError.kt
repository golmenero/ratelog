package org.ratelog.user.changemetadataLang

sealed interface ChangeMetadataLangHandlerError {
    data object UserNotFound : ChangeMetadataLangHandlerError
}
