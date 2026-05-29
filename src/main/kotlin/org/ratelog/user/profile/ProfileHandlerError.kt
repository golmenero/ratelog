package org.ratelog.user.profile

sealed interface ProfileHandlerError {
    data object UserNotFound : ProfileHandlerError
}
