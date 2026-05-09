package org.raterr.user

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.NoSuchElementException

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun getCurrentUserId(): Long? {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication?.name ?: return null
        return userRepository.findByUsername(username).orElse(null)?.id
    }

    fun getCurrentUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication?.name ?: return null
        return userRepository.findByUsername(username).orElse(null)
    }
}
