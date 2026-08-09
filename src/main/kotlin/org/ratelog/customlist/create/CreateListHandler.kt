package org.ratelog.customlist.create

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

sealed interface CreateListError {
    data object InvalidListName : CreateListError
    data object ListLimitExceeded : CreateListError
}

data class CreateListCommand(
    val userId: User.Id,
    val name: String,
    val isPublic: Boolean
)

@Service
class CreateListHandler(
    private val customListRepository: CustomListRepository
) {
    @Transactional
    fun handle(command: CreateListCommand): Either<CreateListError, CustomList> = either {
        val listName = ListName.parse(command.name).mapLeft { CreateListError.InvalidListName }.bind()

        val lists = customListRepository.findByUserId(command.userId)
        if (lists.size >= MAX_LISTS_PER_USER) {
            raise(CreateListError.ListLimitExceeded)
        }

        val newList = CustomList(
            id = null,
            userId = command.userId,
            name = listName,
            isPublic = command.isPublic,
            createdAtEpochMs = System.currentTimeMillis()
        )

        customListRepository.save(newList)
    }


    companion object {
        const val MAX_LISTS_PER_USER = 50
    }
}
