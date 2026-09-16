package org.ratelog.admin.createuser

sealed interface AdminCreateUserHandlerError {
    data object Forbidden : AdminCreateUserHandlerError
    data object UsernameAlreadyExists : AdminCreateUserHandlerError
    data object EmailAlreadyExists : AdminCreateUserHandlerError
    data object CannotPromoteToSuperadmin : AdminCreateUserHandlerError
}
