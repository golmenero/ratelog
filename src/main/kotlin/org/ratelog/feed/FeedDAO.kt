package org.ratelog.feed

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Repository as SpringRepository

data class FeedItemEntity(
    val tmdbId: Int,
    val title: String,
    val score: Double?,
    val text: String?,
    val username: String,
    val date: Long,
    val mediaType: String,
    val seasonNumber: Int?,
)

class FeedMarker

@SpringRepository
interface FeedDAO : Repository<FeedMarker, Long> {

    @Query(
        """
        SELECT
            m.tmdb_id AS tmdb_id,
            m.title AS title,
            mr.score AS score,
            mr.review_text AS text,
            u.username AS username,
            mr.created_at_epoch_ms AS date,
            'movie' AS media_type,
            NULL AS season_number
        FROM movie_ratings mr
        JOIN movies m ON mr.movie_id = m.id
        JOIN users u ON mr.user_id = u.id
        WHERE u.id IN (:userIds)

        UNION ALL

        SELECT
            t.tmdb_id AS tmdb_id,
            t.name AS title,
            sr.score AS score,
            sr.review_text AS text,
            u.username AS username,
            sr.created_at_epoch_ms AS date,
            'tvshow' AS media_type,
            sr.season_number AS season_number
        FROM season_ratings sr
        JOIN tv t ON t.id = sr.tv_show_id
        JOIN users u ON u.id = sr.user_id
        WHERE u.id IN (:userIds)

        ORDER BY date DESC
        LIMIT :limit
        """
    )
    fun findAll(userIds: List<Long>, limit: Int): List<FeedItemEntity>

    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT mr.id
            FROM movie_ratings mr
            JOIN users u ON mr.user_id = u.id
            WHERE u.id IN (:userIds)

            UNION ALL

            SELECT sr.id
            FROM season_ratings sr
            JOIN users u ON u.id = sr.user_id
            WHERE u.id IN (:userIds)
        ) AS combined
        """
    )
    fun count(userIds: List<Long>): Long
}