package org.ratelog.user.update

sealed interface UpdateUserHandlerError {
    data object Forbidden : UpdateUserHandlerError
    data object UserNotFound : UpdateUserHandlerError
    data object UsernameAlreadyExists : UpdateUserHandlerError
    data object EmailAlreadyExists : UpdateUserHandlerError
    data object CannotPromoteToSuperadmin : UpdateUserHandlerError
    data object CannotChangeSuperadminRole : UpdateUserHandlerError
    data object CannotDemoteYourself : UpdateUserHandlerError
}