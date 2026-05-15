package org.raterr.friendship

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FriendshipRepository : CrudRepository<Friendship, Long> {

    fun existsByUserIdAndFriendId(userId: Long, friendId: Long): Boolean

    @Query(
        """
        SELECT f.* FROM friendships f
        WHERE f.user_id = :requesterId
          AND f.friend_id = :receiverId
          AND f.status = 'pending'
        """
    )
    fun findPendingRequest(requesterId: Long, receiverId: Long): Optional<Friendship>

    @Query(
        """
        SELECT u.id, u.username FROM users u
        WHERE u.id IN (
            SELECT f.friend_id FROM friendships f WHERE f.user_id = :userId AND f.status = 'accepted'
            UNION
            SELECT f.user_id FROM friendships f WHERE f.friend_id = :userId AND f.status = 'accepted'
        )
        ORDER BY u.username
        """
    )
    fun findAllFriendsByUserId(userId: Long): List<Friendship>
}
