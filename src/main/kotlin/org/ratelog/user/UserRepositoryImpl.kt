package org.ratelog.user

import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.Username
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class UserRepositoryImpl(
    private val userDAO: UserDAO,
    private val userFollowDAO: UserFollowDAO,
) : UserRepository {
    override fun findById(id: User.Id): User? =
        id.value.let(userDAO::findById).getOrNull()?.toDomain()

    override fun findByUsername(username: Username): User? =
        userDAO.findByUsername(username.value).getOrNull()?.toDomain()

    override fun findByEmail(email: Email): User? =
        userDAO.findByEmail(email.value).getOrNull()?.toDomain()

    override fun save(user: User) {
        user.toEntity().let(userDAO::save)
    }

    override fun findByUsernameContaining(username: Username): List<User> =
        userDAO.findByUsernameContaining(username.value).map { it.toDomain() }

    override fun findByUsernameContaining(username: Username, followerId: User.Id): List<User> =
        userDAO.findByUsernameContaining(username.value).map { it.toDomain() }

    override fun findFollowingByUserId(userId: User.Id): List<User> =
        userFollowDAO.findFollowingUsers(userId.value).map { it.toDomain() }

    override fun findFollowedUserIds(userId: User.Id): List<User.Id> =
        userFollowDAO.findFollowedUserIds(userId.value).map { User.Id(it) }

    override fun isFollowing(followerId: User.Id, followedId: User.Id): Boolean =
        userFollowDAO.findByFollowerIdAndFollowedId(followerId.value, followedId.value).isPresent

    override fun toggleFollow(followerId: User.Id, followedId: User.Id) {
        val existing = userFollowDAO.findByFollowerIdAndFollowedId(followerId.value, followedId.value).getOrNull()
        if (existing == null) {
            UserFollowEntity(followerId = followerId.value, followedId = followedId.value).let(userFollowDAO::save)
        } else {
            userFollowDAO.delete(existing)
        }
    }

    private fun UserEntity.toDomain(): User =
        User(
            id = id!!.let { User.Id(it) },
            username = username.let(::Username),
            email = email.let(::Email),
            passwordHash = passwordHash,
            createdAtEpochMs = createdAtEpochMs,
            lang = Lang.valueOf(lang),
        )

    private fun User.toEntity(): UserEntity =
        UserEntity(
            id = id?.value,
            username = username.value,
            email = email.value,
            passwordHash = passwordHash,
            createdAtEpochMs = createdAtEpochMs,
            lang = lang.name
        )
}
