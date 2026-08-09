package org.ratelog.customlist.removeitem

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListItem
import org.ratelog.test.InMemoryCustomListRepository
import org.ratelog.user.User

class RemoveFromListHandlerTest {

    private lateinit var customListRepository: InMemoryCustomListRepository
    private lateinit var handler: RemoveFromListHandler

    @BeforeEach
    fun setUp() {
        customListRepository = InMemoryCustomListRepository()
        handler = RemoveFromListHandler(customListRepository)
    }

    @Test
    fun `should remove item from list successfully`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val item = customListRepository.addItem(
            CustomListItem(
                id = null,
                listId = list.id!!,
                mediaId = 550L,
                mediaType = org.ratelog.MediaType.movie,
                position = 1,
                addedAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = RemoveFromListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            itemId = item.id!!
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedList = customListRepository.findById(list.id!!)!!
        assertEquals(0, updatedList.items.size)
    }

    @Test
    fun `should return ListNotFound when list does not exist`() {
        val command = RemoveFromListCommand(
            listId = CustomList.Id(999),
            userId = User.Id(1),
            itemId = CustomListItem.Id(1)
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(RemoveFromListError.ListNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return NotOwner when user is not the owner`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val item = customListRepository.addItem(
            CustomListItem(
                id = null,
                listId = list.id!!,
                mediaId = 550L,
                mediaType = org.ratelog.MediaType.movie,
                position = 1,
                addedAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = RemoveFromListCommand(
            listId = list.id!!,
            userId = User.Id(2),
            itemId = item.id!!
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(RemoveFromListError.NotOwner, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return ItemNotFound when item does not exist`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = RemoveFromListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            itemId = CustomListItem.Id(999)
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(RemoveFromListError.ItemNotFound, result.fold({ it }, { fail("Should not return success") }))
    }
}
