package org.ratelog.user.listusers

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Component

data class ListUsersQuery(
    val currentUser: User,
)

@Component
class ListUsersHandler(
    private val userRepository: UserRepository,
) {
    fun handle(query: ListUsersQuery): Either<ListUsersHandlerError, List<User>> = either {
        ensure(query.currentUser.role.isAdminLike) { ListUsersHandlerError.Forbidden }
        userRepository.findAll()
    }
}