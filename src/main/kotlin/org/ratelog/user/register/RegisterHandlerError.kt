package org.ratelog.user.register

sealed interface RegisterHandlerError {
    data object UsernameAlreadyExists : RegisterHandlerError
    data object EmailAlreadyExists : RegisterHandlerError
}
