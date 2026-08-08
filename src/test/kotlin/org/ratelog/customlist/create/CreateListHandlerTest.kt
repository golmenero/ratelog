package org.ratelog.customlist.create

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.test.InMemoryCustomListRepository
import org.ratelog.user.User

class CreateListHandlerTest {

    private lateinit var customListRepository: InMemoryCustomListRepository
    private lateinit var handler: CreateListHandler

    @BeforeEach
    fun setUp() {
        customListRepository = InMemoryCustomListRepository()
        handler = CreateListHandler(customListRepository)
    }

    @Test
    fun `should create list successfully`() {
        val command = CreateListCommand(
            userId = User.Id(1),
            name = "My Favorites",
            isPublic = true
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { list ->
                assertNotNull(list.id)
                assertEquals("My Favorites", list.name.value)
                assertTrue(list.isPublic)
            }
        )
    }

    @Test
    fun `should return InvalidListName when name is empty`() {
        val command = CreateListCommand(
            userId = User.Id(1),
            name = "",
            isPublic = false
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(CreateListError.InvalidListName, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return InvalidListName when name is too long`() {
        val command = CreateListCommand(
            userId = User.Id(1),
            name = "a".repeat(101),
            isPublic = false
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(CreateListError.InvalidListName, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return ListLimitExceeded when user has 50 lists`() {
        repeat(50) { i ->
            customListRepository.save(
                org.ratelog.customlist.CustomList(
                    id = null,
                    userId = User.Id(1),
                    name = org.ratelog.ListName("List $i"),
                    isPublic = false,
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        }

        val command = CreateListCommand(
            userId = User.Id(1),
            name = "New List",
            isPublic = false
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(CreateListError.ListLimitExceeded, result.fold({ it }, { fail("Should not return success") }))
    }
}
