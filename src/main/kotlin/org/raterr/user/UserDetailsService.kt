package org.raterr.user

import org.raterr.Email
import org.raterr.Username
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

class AppUserDetails(
    val id: User.Id,
    private val username: String,
    val email: String,
    private val password: String,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

@Service
class UserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): AppUserDetails {
        val user = userRepository.findByUsername(username.let(::Username))
            ?: throw UsernameNotFoundException("User not found: $username")

        return AppUserDetails(
            id = user.id!!,
            username = user.username.value,
            email = user.email.value,
            password = user.passwordHash,
        )
    }

    companion object {
        fun getCurrentUser(): User? {
            val p = SecurityContextHolder.getContext().authentication?.principal as? AppUserDetails ?: return null
            return User(
                id = p.id,
                username = Username(p.username),
                email = Email(p.email),
                passwordHash = p.password,
            )
        }
    }
}