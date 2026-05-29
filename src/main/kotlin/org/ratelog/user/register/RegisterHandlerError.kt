package org.ratelog.user.register

sealed interface RegisterHandlerError {
    data object EmptyFields : RegisterHandlerError
    data object InvalidUsernameLength : RegisterHandlerError
    data object InvalidPasswordLength : RegisterHandlerError
    data object UsernameAlreadyExists : RegisterHandlerError
    data object EmailAlreadyExists : RegisterHandlerError
}
