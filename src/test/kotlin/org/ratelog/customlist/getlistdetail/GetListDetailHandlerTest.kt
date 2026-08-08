package org.ratelog.customlist.getlistdetail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.ListName
import org.ratelog.customlist.CustomList
import org.ratelog.test.InMemoryCustomListRepository
import org.ratelog.test.InMemoryMovieRepository
import org.ratelog.test.InMemoryTvShowRepository
import org.ratelog.user.User

class GetListDetailHandlerTest {

    private lateinit var customListRepository: InMemoryCustomListRepository
    private lateinit var movieRepository: InMemoryMovieRepository
    private lateinit var tvShowRepository: InMemoryTvShowRepository
    private lateinit var handler: GetListDetailHandler

    @BeforeEach
    fun setUp() {
        customListRepository = InMemoryCustomListRepository()
        movieRepository = InMemoryMovieRepository()
        tvShowRepository = InMemoryTvShowRepository()
        handler = GetListDetailHandler(customListRepository, movieRepository, tvShowRepository)
    }

    @Test
    fun `should get list detail successfully when list is public`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("Public List"),
                isPublic = true,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val query = GetListDetailQuery(list.id!!, User.Id(2))

        val result = handler.handle(query)

        assertTrue(result.isRight())
        result.fold(
            { fail("Should not return error") },
            { retrievedList ->
                assertEquals(list.id, retrievedList.id)
                assertEquals("Public List", retrievedList.name)
            }
        )
    }

    @Test
    fun `should get list detail successfully when user is owner`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("Private List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val query = GetListDetailQuery(list.id!!, User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isRight())
    }

    @Test
    fun `should return ListNotFound when list does not exist`() {
        val query = GetListDetailQuery(CustomList.Id(999), User.Id(1))

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertEquals(GetListDetailError.ListNotFound, result.fold({ it }, { fail("Should not return success") }))
    }

    @Test
    fun `should return NotAuthorized when list is private and user is not owner`() {
        val list = customListRepository.save(
            CustomList(
                id = null,
                userId = User.Id(1),
                name = ListName("Private List"),
                isPublic = false,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )

        val query = GetListDetailQuery(list.id!!, User.Id(2))

        val result = handler.handle(query)

        assertTrue(result.isLeft())
        assertEquals(GetListDetailError.NotAuthorized, result.fold({ it }, { fail("Should not return success") }))
    }
}
