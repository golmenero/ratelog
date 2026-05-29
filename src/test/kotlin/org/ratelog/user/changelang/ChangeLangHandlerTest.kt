package org.ratelog.user.changelang

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Lang
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class ChangeLangHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: ChangeLangHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = ChangeLangHandler(userRepository)
    }

    @Test
    fun `should update user lang when user exists`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            lang = Lang("es"),
        )
        userRepository.save(user)

        val command = ChangeLangCommand(
            userId = User.Id(1),
            lang = Lang("en"),
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedUser = userRepository.findById(User.Id(1))
        assertEquals("en", updatedUser!!.lang.value)
    }

    @Test
    fun `should return UserNotFound when user does not exist`() {
        val command = ChangeLangCommand(
            userId = User.Id(999),
            lang = Lang("en"),
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(ChangeLangHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }
}
