package org.ratelog.user.listusers

sealed interface ListUsersHandlerError {
    data object Forbidden : ListUsersHandlerError
}