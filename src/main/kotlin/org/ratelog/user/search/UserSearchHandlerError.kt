package org.ratelog.user.search

sealed interface UserSearchHandlerError {
    data class NoUsersFound(val username: String) : UserSearchHandlerError
}
