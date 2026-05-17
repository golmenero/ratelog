package org.raterr.user.profile

sealed interface ProfileHandlerError {
    data object UserNotFound : ProfileHandlerError
}
