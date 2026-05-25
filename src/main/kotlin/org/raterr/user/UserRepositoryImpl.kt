package org.raterr.user

import org.raterr.Email
import org.raterr.Username
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class UserRepositoryImpl(private val userDAO: UserDAO) : UserRepository {
    override fun findById(id: User.Id): User? =
        id.value.let(userDAO::findById).getOrNull()?.toDomain()

    override fun findByUsername(username: Username): User? =
        userDAO.findByUsername(username.value).getOrNull()?.toDomain()

    override fun findByEmail(email: Email): User? =
        userDAO.findByEmail(email.value).getOrNull()?.toDomain()

    override fun existsByUsername(username: Username): Boolean =
        userDAO.existsByUsername(username.value)

    override fun existsByEmail(email: Email): Boolean =
        userDAO.existsByEmail(email.value)

    override fun save(user: User): User =
        user.toEntity().let(userDAO::save).toDomain()

    override fun findByUsernameContaining(username: Username): List<User> =
        userDAO.findByUsernameContaining(username.value).map { it.toDomain() }

    private fun UserEntity.toDomain(): User =
        User(
            id = id?.let { User.Id(it) },
            username = username.let(::Username),
            email = email.let(::Email),
            passwordHash = passwordHash,
            createdAtEpochMs = createdAtEpochMs
        )

    private fun User.toEntity(): UserEntity =
        UserEntity(
            id = id?.value,
            username = username.value,
            email = email.value,
            passwordHash = passwordHash,
            createdAtEpochMs = createdAtEpochMs
        )
}
