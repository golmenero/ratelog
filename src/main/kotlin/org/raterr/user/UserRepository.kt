package org.raterr.user

import org.raterr.Email
import org.raterr.Username

interface UserRepository {
    fun findById(id: User.Id): User?
    fun findByUsername(username: Username): User?
    fun findByEmail(email: Email): User?
    fun existsByUsername(username: Username): Boolean
    fun existsByEmail(email: Email): Boolean
    fun save(user: User): User
    fun findByUsernameContaining(username: Username): List<User>
}
