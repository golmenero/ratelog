package org.ratelog.customlist.additem

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.test.InMemoryCustomListRepository
import org.ratelog.user.User

class AddToListHandlerTest {

    private lateinit var customListRepository: InMemoryCustomListRepository
    private lateinit var handler: AddToListHandler

    @BeforeEach
    fun setUp() {
        customListRepository = InMemoryCustomListRepository()
        handler = AddToListHandler(customListRepository)
    }

    @Test
    fun `should add item to list successfully`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = AddToListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            tmdbId = 550,
            mediaType = "movie"
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedList = customListRepository.findById(list.id!!)!!
        assertEquals(1, updatedList.items.size)
        assertEquals(550, updatedList.items[0].tmdbId.value)
    }

    @Test
    fun `should return ListNotFound when list does not exist`() {
        val command = AddToListCommand(
            listId = CustomList.Id(999),
            userId = User.Id(1),
            tmdbId = 550,
            mediaType = "movie"
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(AddToListError.ListNotFound, result.fold({ it }, { fail("Should not return success") }))
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

        val command = AddToListCommand(
            listId = list.id!!,
            userId = User.Id(2),
            tmdbId = 550,
            mediaType = "movie"
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(AddToListError.NotOwner, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return ItemAlreadyInList when item already exists`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val command1 = AddToListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            tmdbId = 550,
            mediaType = "movie"
        )
        handler.handle(command1)

        val command2 = AddToListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            tmdbId = 550,
            mediaType = "movie"
        )

        val result = handler.handle(command2)

        assertTrue(result.isLeft())
        assertEquals(AddToListError.ItemAlreadyInList, result.fold({ it }, { fail("Should not return success") }))
    }
}
