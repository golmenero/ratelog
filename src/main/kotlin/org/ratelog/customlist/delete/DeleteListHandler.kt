package org.ratelog.customlist.delete

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

sealed interface DeleteListError {
    data object ListNotFound : DeleteListError
    data object NotOwner : DeleteListError
}

data class DeleteListCommand(
    val listId: CustomList.Id,
    val userId: User.Id
)

@Service
class DeleteListHandler(
    private val customListRepository: CustomListRepository
) {
    @Transactional
    fun handle(command: DeleteListCommand): Either<DeleteListError, Unit> = either {
        val list = customListRepository.findById(command.listId)
            ?: raise(DeleteListError.ListNotFound)

        if (list.userId != command.userId) {
            raise(DeleteListError.NotOwner)
        }

        customListRepository.delete(command.listId)
    }
}
