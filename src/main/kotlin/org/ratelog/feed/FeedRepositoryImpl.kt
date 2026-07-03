package org.ratelog.feed

import org.ratelog.MediaType
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Username
import org.ratelog.user.User
import org.springframework.stereotype.Repository

@Repository
class FeedRepositoryImpl(
        private val feedDAO: FeedDAO,
    ) : FeedRepository {
    override fun findAll(userIds: List<User.Id>, limit: Int): List<FeedItem> =
        feedDAO.findAll(userIds.map { it.value }, limit).map { it.toDomain() }

    override fun count(userIds: List<User.Id>): Long =
        feedDAO.count(userIds.map { it.value })

    private fun FeedItemEntity.toDomain() = FeedItem(
        tmdbId = tmdbId.let(::TmdbId),
        title = title.let(::Title),
        score = score?.let(::Score),
        username = username.let(::Username),
        text = text,
        createdAtEpochMs = date,
        mediaType = MediaType.valueOf(mediaType),
        seasonNumber = seasonNumber?.let(::SeasonNumber)
    )
}
