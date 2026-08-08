package org.ratelog.customlist.additem

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.MediaType
import org.ratelog.TmdbId
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListItem
import org.ratelog.customlist.CustomListRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

sealed interface AddToListError {
    data object ListNotFound : AddToListError
    data object NotOwner : AddToListError
    data object ItemAlreadyInList : AddToListError
}

data class AddToListCommand(
    val listId: CustomList.Id,
    val userId: User.Id,
    val tmdbId: Int,
    val mediaType: String
)

@Service
class AddToListHandler(
    private val customListRepository: CustomListRepository
) {
    @Transactional
    fun handle(command: AddToListCommand): Either<AddToListError, Unit> = either {
        val list = customListRepository.findById(command.listId)
            ?: raise(AddToListError.ListNotFound)

        if (list.userId != command.userId) {
            raise(AddToListError.NotOwner)
        }

        val tmdbId = TmdbId(command.tmdbId)
        val mediaType = MediaType.valueOf(command.mediaType)

        val existingItem = customListRepository.findItemByListIdAndTmdbIdAndMediaType(
            command.listId,
            command.tmdbId,
            command.mediaType
        )

        if (existingItem != null) {
            raise(AddToListError.ItemAlreadyInList)
        }

        val maxPosition = list.items.maxOfOrNull { it.position } ?: 0

        val newItem = CustomListItem(
            id = null,
            listId = command.listId,
            tmdbId = tmdbId,
            mediaType = mediaType,
            position = maxPosition + 1,
            addedAtEpochMs = System.currentTimeMillis()
        )

        customListRepository.addItem(newItem)
    }
}
