package org.ratelog.customlist.removeitem

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListItem
import org.ratelog.customlist.CustomListRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

sealed interface RemoveFromListError {
    data object ListNotFound : RemoveFromListError
    data object NotOwner : RemoveFromListError
    data object ItemNotFound : RemoveFromListError
}

data class RemoveFromListCommand(
    val listId: CustomList.Id,
    val userId: User.Id,
    val itemId: CustomListItem.Id
)

@Service
class RemoveFromListHandler(
    private val customListRepository: CustomListRepository
) {
    @Transactional
    fun handle(command: RemoveFromListCommand): Either<RemoveFromListError, Unit> = either {
        val list = customListRepository.findById(command.listId)
            ?: raise(RemoveFromListError.ListNotFound)

        if (list.userId != command.userId) {
            raise(RemoveFromListError.NotOwner)
        }

        val item = customListRepository.findItemById(command.itemId)
            ?: raise(RemoveFromListError.ItemNotFound)

        if (item.listId != command.listId) {
            raise(RemoveFromListError.ItemNotFound)
        }

        customListRepository.removeItem(command.itemId)
    }
}
