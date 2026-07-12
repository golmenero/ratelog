package org.ratelog.user.changemetadataLang

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Lang
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.User

class ChangeMetadataLangHandlerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var handler: ChangeMetadataLangHandler

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        handler = ChangeMetadataLangHandler(userRepository)
    }

    @Test
    fun `should update user metadata lang when user exists`() {
        val user = UserFactory.aUser(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            lang = Lang.es,
            metadataLang = Lang.en,
        )
        userRepository.save(user)

        val command = ChangeMetadataLangCommand(
            userId = User.Id(1),
            metadataLang = Lang.ja,
        )

        val result = handler.handle(command)

        assertTrue(result.isRight())
        val updatedUser = userRepository.findById(User.Id(1))
        assertEquals("ja", updatedUser!!.metadataLang.name)
    }

    @Test
    fun `should return UserNotFound when user does not exist`() {
        val command = ChangeMetadataLangCommand(
            userId = User.Id(999),
            metadataLang = Lang.en,
        )

        val result = handler.handle(command)

        assertTrue(result.isLeft())
        assertEquals(ChangeMetadataLangHandlerError.UserNotFound, result.fold({ it }, { Unit }))
    }
}
