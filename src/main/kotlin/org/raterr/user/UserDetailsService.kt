package org.raterr.user

import org.raterr.Username
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username.let(::Username))
            ?: throw UsernameNotFoundException("User not found: $username")

        return org.springframework.security.core.userdetails.User(
            user.username.value,
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }

    fun getCurrentUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication?.name?.let(::Username) ?: return null
        return userRepository.findByUsername(username)
    }
}
