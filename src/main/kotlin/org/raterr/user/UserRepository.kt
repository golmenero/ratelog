package org.raterr.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
