package org.ratelog.feed

import org.ratelog.MediaType
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Username
import org.ratelog.user.User
import org.springframework.stereotype.Repository

data class FeedItem(
    val tmdbId: TmdbId,
    val title: Title,
    val score: Score?,
    val text: String?,
    val username: Username,
    val createdAtEpochMs: Long,
    val mediaType: MediaType,
    val seasonNumber: SeasonNumber?,
)

@Repository
interface FeedRepository {
    fun findAll(userIds: List<User.Id>, limit: Int): List<FeedItem>

    fun count(userIds: List<User.Id>): Long
}
