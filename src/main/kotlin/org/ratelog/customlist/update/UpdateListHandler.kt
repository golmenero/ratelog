package org.ratelog.customlist.update

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

sealed interface UpdateListError {
    data object ListNotFound : UpdateListError
    data object NotOwner : UpdateListError
    data object InvalidListName : UpdateListError
}

data class UpdateListCommand(
    val listId: CustomList.Id,
    val userId: User.Id,
    val name: String,
    val isPublic: Boolean
)

@Service
class UpdateListHandler(
    private val customListRepository: CustomListRepository
) {
    @Transactional
    fun handle(command: UpdateListCommand): Either<UpdateListError, Unit> = either {
        val list = customListRepository.findById(command.listId)
            ?: raise(UpdateListError.ListNotFound)

        if (list.userId != command.userId) {
            raise(UpdateListError.NotOwner)
        }

        val listName = ListName.parse(command.name).mapLeft { UpdateListError.InvalidListName }.bind()

        list.copy(
            name = listName,
            isPublic = command.isPublic
        ).let(customListRepository::save)
    }
}
