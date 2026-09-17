package org.ratelog.user.create

sealed interface CreateUserHandlerError {
    data object Forbidden : CreateUserHandlerError
    data object UsernameAlreadyExists : CreateUserHandlerError
    data object EmailAlreadyExists : CreateUserHandlerError
    data object CannotPromoteToSuperadmin : CreateUserHandlerError
}