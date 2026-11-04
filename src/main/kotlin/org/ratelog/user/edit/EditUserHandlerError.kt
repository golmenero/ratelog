package org.ratelog.user.edit

sealed interface EditUserHandlerError {
    data object Forbidden : EditUserHandlerError
    data object UserNotFound : EditUserHandlerError
    data object UsernameAlreadyExists : EditUserHandlerError
    data object EmailAlreadyExists : EditUserHandlerError
    data object MissingCurrentPassword : EditUserHandlerError
    data object InvalidCurrentPassword : EditUserHandlerError
    data object CannotEditAdmin : EditUserHandlerError
    data object CannotChangeRole : EditUserHandlerError
    data object CannotPromoteToSuperadmin : EditUserHandlerError
    data object CannotChangeSuperadminRole : EditUserHandlerError
    data object CannotDemoteYourself : EditUserHandlerError
}
