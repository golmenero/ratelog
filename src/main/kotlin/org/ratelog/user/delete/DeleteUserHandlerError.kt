package org.ratelog.user.delete

sealed interface DeleteUserHandlerError {
    data object Forbidden : DeleteUserHandlerError
    data object UserNotFound : DeleteUserHandlerError
    data object CannotDeleteYourself : DeleteUserHandlerError
    data object CannotDeleteSuperadmin : DeleteUserHandlerError
    data object CannotDeleteAdmin : DeleteUserHandlerError
}