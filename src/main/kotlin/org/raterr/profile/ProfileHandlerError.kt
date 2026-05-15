package org.raterr.profile

sealed interface ProfileHandlerError {
    data object UserNotFound : ProfileHandlerError
}
