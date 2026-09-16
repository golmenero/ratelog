package org.ratelog.admin.listusers

sealed interface ListUsersHandlerError {
    data object Forbidden : ListUsersHandlerError
}
