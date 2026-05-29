package org.ratelog.user.edit

sealed interface EditUserHandlerError {
    data object UserNotFound : EditUserHandlerError
    data object EmptyFields : EditUserHandlerError
    data object InvalidUsernameLength : EditUserHandlerError
    data object InvalidPasswordLength : EditUserHandlerError
    data object UsernameAlreadyExists : EditUserHandlerError
    data object EmailAlreadyExists : EditUserHandlerError
    data object InvalidCurrentPassword : EditUserHandlerError
}
