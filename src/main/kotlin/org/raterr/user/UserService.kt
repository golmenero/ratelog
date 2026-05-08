package org.raterr.user

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.NoSuchElementException

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getCurrentUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication?.name ?: return null
        return userRepository.findByUsername(username).orElse(null)
    }

    fun getRequiredCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name
        return userRepository.findByUsername(username)
            .orElseThrow { NoSuchElementException("User not found") }
    }
}
