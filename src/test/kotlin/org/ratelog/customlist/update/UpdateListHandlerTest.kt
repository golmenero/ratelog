package org.ratelog.customlist.update

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.test.InMemoryCustomListRepository
import org.ratelog.user.User

class UpdateListHandlerTest {

    private lateinit var customListRepository: InMemoryCustomListRepository
    private lateinit var handler: UpdateListHandler

    @BeforeEach
    fun setUp() {
        customListRepository = InMemoryCustomListRepository()
        handler = UpdateListHandler(customListRepository)
    }

    @Test
    fun `should update list successfully`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("Original Name"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = UpdateListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            name = "Updated Name",
            isPublic = true
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedList = customListRepository.findById(list.id!!)!!
        assertEquals("Updated Name", updatedList.name.value)
        assertTrue(updatedList.isPublic)
    }

    @Test
    fun `should return ListNotFound when list does not exist`() {
        val command = UpdateListCommand(
            listId = CustomList.Id(999),
            userId = User.Id(1),
            name = "Updated Name",
            isPublic = false
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(UpdateListError.ListNotFound, result.fold({ it }, { fail("Should not return success") }))
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

        val command = UpdateListCommand(
            listId = list.id!!,
            userId = User.Id(2),
            name = "Updated Name",
            isPublic = false
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(UpdateListError.NotOwner, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return InvalidListName when name is empty`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = UpdateListCommand(
            listId = list.id!!,
            userId = User.Id(1),
            name = "",
            isPublic = false
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(UpdateListError.InvalidListName, result.fold({ it }, { fail("Should not return success") }))
    }
}
