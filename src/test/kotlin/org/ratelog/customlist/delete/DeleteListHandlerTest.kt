package org.ratelog.customlist.delete

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.test.InMemoryCustomListRepository
import org.ratelog.user.User

class DeleteListHandlerTest {

    private lateinit var customListRepository: InMemoryCustomListRepository
    private lateinit var handler: DeleteListHandler

    @BeforeEach
    fun setUp() {
        customListRepository = InMemoryCustomListRepository()
        handler = DeleteListHandler(customListRepository)
    }

    @Test
    fun `should delete list successfully`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("My List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val command = DeleteListCommand(
            listId = list.id!!,
            userId = User.Id(1)
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        assertNull(customListRepository.findById(list.id!!))
    }

    @Test
    fun `should return ListNotFound when list does not exist`() {
        val command = DeleteListCommand(
            listId = CustomList.Id(999),
            userId = User.Id(1)
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(DeleteListError.ListNotFound, result.fold({ it }, { fail("Should not return success") }))
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

        val command = DeleteListCommand(
            listId = list.id!!,
            userId = User.Id(2)
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(DeleteListError.NotOwner, result.fold({ it }, { fail("Should not return success") }))
    }
}
