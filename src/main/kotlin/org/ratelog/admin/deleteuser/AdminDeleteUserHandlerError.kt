package org.ratelog.admin.deleteuser

sealed interface AdminDeleteUserHandlerError {
    data object Forbidden : AdminDeleteUserHandlerError
    data object UserNotFound : AdminDeleteUserHandlerError
    data object CannotDeleteYourself : AdminDeleteUserHandlerError
    data object CannotDeleteSuperadmin : AdminDeleteUserHandlerError
}
