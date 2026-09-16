package org.ratelog.admin.updatecredentials

sealed interface AdminUpdateCredentialsHandlerError {
    data object Forbidden : AdminUpdateCredentialsHandlerError
    data object UserNotFound : AdminUpdateCredentialsHandlerError
    data object UsernameAlreadyExists : AdminUpdateCredentialsHandlerError
}
