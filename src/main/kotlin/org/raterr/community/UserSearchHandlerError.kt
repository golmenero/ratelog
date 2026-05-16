package org.raterr.community

sealed interface UserSearchHandlerError {
    data object EmptyQuery : UserSearchHandlerError
    data class NoUsersFound(val username: String) : UserSearchHandlerError
}
