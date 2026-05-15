package org.raterr.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.raterr.user.InMemoryUserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException

class UserDetailsServiceTest {

    private val userRepository = InMemoryUserRepository()
    private val service = UserDetailsService(userRepository)

    @BeforeEach
    fun setUp() {
        userRepository.clear()
    }

    @Test
    fun `loadUserByUsername success returns UserDetails with ROLE_USER`() {
        userRepository.save(User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hashed"))

        val userDetails = service.loadUserByUsername("testuser")

        assertEquals("testuser", userDetails.username)
        assertEquals("hashed", userDetails.password)
        assertEquals(1, userDetails.authorities.size)
        assertEquals("ROLE_USER", userDetails.authorities.first().authority)
    }

    @Test
    fun `loadUserByUsername not found throws UsernameNotFoundException`() {
        assertThrows(UsernameNotFoundException::class.java) {
            service.loadUserByUsername("unknown")
        }
    }

    @Test
    fun `getCurrentUser success returns current user`() {
        userRepository.save(User(id = 1, username = "testuser", email = "test@example.com", passwordHash = "hashed"))
        val auth = UsernamePasswordAuthenticationToken("testuser", null, listOf(SimpleGrantedAuthority("ROLE_USER")))
        SecurityContextHolder.getContext().authentication = auth

        val result = service.getCurrentUser()

        assertNotNull(result)
        assertEquals("testuser", result?.username)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `getCurrentUser no auth returns null`() {
        SecurityContextHolder.clearContext()

        val result = service.getCurrentUser()

        assertNull(result)
    }
}
