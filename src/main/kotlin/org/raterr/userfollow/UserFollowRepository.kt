package org.raterr.userfollow

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserFollowRepository : CrudRepository<UserFollow, Long> {

    fun existsByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Boolean

    fun findByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Optional<UserFollow>

    @Query(
        """
        SELECT fu.id, fu.follower_id, fu.followed_id, fu.created_at_epoch_ms, u.username AS followed_username
        FROM follows_users fu
        JOIN users u ON u.id = fu.followed_id
        WHERE fu.follower_id = :userId
        ORDER BY u.username
        """
    )
    fun findFollowingByUserId(userId: Long): List<UserFollowWithUsername>

    @Query(
        """
        SELECT u.id, u.username FROM users u
        WHERE u.id IN (
            SELECT f.follower_id FROM follows_users f WHERE f.followed_id = :userId
        )
        ORDER BY u.username
        """
    )
    fun findFollowersByUserId(userId: Long): List<UserFollow>

    @Query(
        """
        SELECT fu.followed_id FROM follows_users fu WHERE fu.follower_id = :userId
        """
    )
    fun findFollowedUserIds(userId: Long): List<Long>
}

data class UserFollowWithUsername(
    val id: Long?,
    val followerId: Long,
    val followedId: Long,
    val createdAtEpochMs: Long,
    val followedUsername: String
)
