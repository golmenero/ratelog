package org.ratelog.user.edit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Role
import org.ratelog.test.FakePasswordEncoder
import org.ratelog.test.InMemoryUserRepository
import org.ratelog.test.UserFactory
import org.ratelog.user.AppUserDetails
import org.ratelog.user.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap

class EditUserControllerTest {

    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var controller: EditUserController

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepository()
        controller = EditUserController(EditUserHandler(userRepository, FakePasswordEncoder()))
        SecurityContextHolder.clearContext()
    }

    private fun authenticate(user: User) {
        val details = AppUserDetails(
            id = user.id!!,
            username = user.username.value,
            email = user.email.value,
            password = user.passwordHash,
            lang = user.lang,
            metadataLang = user.metadataLang,
            role = user.role,
        )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            details,
            null,
            listOf(SimpleGrantedAuthority(user.role.authority)),
        )
    }

    @Test
    fun `given a user editing their profile with the right current password then data is updated and no error is flashed`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal", email = "old@example.com"))
        authenticate(user)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editProfile(user, "renamed", "new@example.com", "password", null, redirectAttributes)

        // Then
        assertEquals("redirect:/profile/${user.id!!.value}", view)
        assertNull(redirectAttributes.getFlashAttributes()["error"])
        val persisted = userRepository.findById(user.id!!)!!
        assertEquals("renamed", persisted.username.value)
        assertEquals("new@example.com", persisted.email.value)
    }

    @Test
    fun `given a user editing their profile without current password then missing current password error is flashed`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))
        authenticate(user)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editProfile(user, "normal", user.email.value, null, "NewPass1!", redirectAttributes)

        // Then
        assertEquals("redirect:/profile/${user.id!!.value}", view)
        assertEquals("edit.error.missing.current.password", redirectAttributes.getFlashAttributes()["error"])
        assertEquals("encoded_password", userRepository.findById(user.id!!)!!.passwordHash)
    }

    @Test
    fun `given a user editing their profile with a blank current password then missing current password error is flashed`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))
        authenticate(user)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editProfile(user, "normal", user.email.value, "", "NewPass1!", redirectAttributes)

        // Then
        assertEquals("redirect:/profile/${user.id!!.value}", view)
        assertEquals("edit.error.missing.current.password", redirectAttributes.getFlashAttributes()["error"])
        assertEquals("encoded_password", userRepository.findById(user.id!!)!!.passwordHash)
    }

    @Test
    fun `given a user editing their profile with a wrong current password then invalid current password error is flashed`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))
        authenticate(user)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editProfile(user, "normal", user.email.value, "wrongpassword", null, redirectAttributes)

        // Then
        assertEquals("redirect:/profile/${user.id!!.value}", view)
        assertEquals("edit.error.invalid.current.password", redirectAttributes.getFlashAttributes()["error"])
    }

    @Test
    fun `given a weak new password then it is updated`() {
        // Given
        val user = userRepository.save(UserFactory.aUser(username = "normal"))
        authenticate(user)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editProfile(user, "normal", user.email.value, "password", "abc", redirectAttributes)

        // Then
        assertEquals("redirect:/profile/${user.id!!.value}", view)
        assertNull(redirectAttributes.getFlashAttributes()["error"])
        assertEquals("encoded_abc", userRepository.findById(user.id!!)!!.passwordHash)
    }

    @Test
    fun `given a superadmin editing their profile without current password then password is updated and no error is flashed`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        authenticate(superadmin)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editProfile(superadmin, "root", superadmin.email.value, null, "NewPass1!", redirectAttributes)

        // Then
        assertEquals("redirect:/profile/${superadmin.id!!.value}", view)
        assertNull(redirectAttributes.getFlashAttributes()["error"])
        val persisted = userRepository.findById(superadmin.id!!)!!
        assertEquals("encoded_NewPass1!", persisted.passwordHash)
        assertEquals(Role.SUPERADMIN, persisted.role)
    }

    @Test
    fun `given a superadmin editing their profile then the session principal is refreshed`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        authenticate(superadmin)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        controller.editProfile(superadmin, "root", superadmin.email.value, null, "NewPass1!", redirectAttributes)

        // Then
        val principal = SecurityContextHolder.getContext().authentication.principal as AppUserDetails
        assertEquals("encoded_NewPass1!", principal.password)
        assertEquals(Role.SUPERADMIN, principal.role)
    }

    @Test
    fun `given a superadmin editing a user in the panel without role then password is updated and role is kept`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", role = Role.SUPERADMIN))
        authenticate(superadmin)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editUser(
            superadmin,
            target.id!!.value,
            target.username.value,
            target.email.value,
            null,
            "NewPass1!",
            null,
            redirectAttributes,
        )

        // Then
        assertEquals("redirect:/admin/configuration", view)
        assertNull(redirectAttributes.getFlashAttributes()["error"])
        val persisted = userRepository.findById(target.id!!)!!
        assertEquals("encoded_NewPass1!", persisted.passwordHash)
        assertEquals(Role.SUPERADMIN, persisted.role)
    }

    @Test
    fun `given an admin editing a user in the panel without current password then missing current password error is flashed`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim"))
        authenticate(admin)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editUser(
            admin,
            target.id!!.value,
            target.username.value,
            target.email.value,
            null,
            "NewPass1!",
            null,
            redirectAttributes,
        )

        // Then
        assertEquals("redirect:/admin/configuration", view)
        assertEquals("edit.error.missing.current.password", redirectAttributes.getFlashAttributes()["error"])
        assertEquals("encoded_password", userRepository.findById(target.id!!)!!.passwordHash)
    }

    @Test
    fun `given an unknown role then invalid role error is flashed and nothing is updated`() {
        // Given
        val admin = userRepository.save(UserFactory.aUser(username = "admin1", role = Role.ADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "victim", email = "old@example.com"))
        authenticate(admin)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editUser(
            admin,
            target.id!!.value,
            target.username.value,
            target.email.value,
            "password",
            null,
            "GOD",
            redirectAttributes,
        )

        // Then
        assertEquals("redirect:/admin/configuration", view)
        assertEquals("admin.error.invalid.role", redirectAttributes.getFlashAttributes()["error"])
        assertEquals("old@example.com", userRepository.findById(target.id!!)!!.email.value)
    }

    @Test
    fun `given a superadmin target with role change then cannot change superadmin role error is flashed`() {
        // Given
        val superadmin = userRepository.save(UserFactory.aUser(username = "root", role = Role.SUPERADMIN))
        val target = userRepository.save(UserFactory.aUser(username = "root2", role = Role.SUPERADMIN))
        authenticate(superadmin)
        val redirectAttributes = RedirectAttributesModelMap()

        // When
        val view = controller.editUser(
            superadmin,
            target.id!!.value,
            target.username.value,
            target.email.value,
            null,
            null,
            "USER",
            redirectAttributes,
        )

        // Then
        assertEquals("redirect:/admin/configuration", view)
        assertEquals("admin.error.cannot.change.superadmin.role", redirectAttributes.getFlashAttributes()["error"])
        assertEquals(Role.SUPERADMIN, userRepository.findById(target.id!!)!!.role)
    }
}
