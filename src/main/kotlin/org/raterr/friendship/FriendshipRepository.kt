package org.raterr.friendship

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FriendshipRepository : CrudRepository<Friendship, Long> {

    fun findByUserIdAndFriendId(userId: Long, friendId: Long): Optional<Friendship>

    fun existsByUserIdAndFriendId(userId: Long, friendId: Long): Boolean

    @Query(
        """
        SELECT f.* FROM friendships f
        WHERE (f.user_id = :userId OR f.friend_id = :userId)
          AND f.status = 'accepted'
        """
    )
    fun findAcceptedByUserId(userId: Long): List<Friendship>

    @Query(
        """
        SELECT f.* FROM friendships f
        WHERE f.user_id = :requesterId
          AND f.friend_id = :receiverId
          AND f.status = 'pending'
        """
    )
    fun findPendingRequest(requesterId: Long, receiverId: Long): Optional<Friendship>

    fun findByUserIdAndStatus(userId: Long, status: String): List<Friendship>

    @Query(
        """
        SELECT f.friend_id FROM friendships f
        WHERE f.user_id = :userId
          AND f.status = 'accepted'
        UNION
        SELECT f.user_id FROM friendships f
        WHERE f.friend_id = :userId
          AND f.status = 'accepted'
        """
    )
    fun findFriendIdsByUserId(userId: Long): List<Long>
}
