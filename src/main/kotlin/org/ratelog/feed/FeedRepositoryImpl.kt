package org.ratelog.feed

import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.Review
import org.ratelog.ReviewText
import org.ratelog.ReviewTitle
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
    override fun findAll(userIds: List<User.Id>, lang: Lang, limit: Int): List<FeedItem> =
        feedDAO.findAll(userIds.map { it.value }, lang.name, limit).map { it.toDomain() }

    override fun count(userIds: List<User.Id>): Long =
        feedDAO.count(userIds.map { it.value })

    private fun FeedItemEntity.toDomain() = FeedItem(
        tmdbId = tmdbId.let(::TmdbId),
        title = Title(title),
        score = score?.let(::Score),
        username = username.let(::Username),
        review = toReview(reviewTitle, reviewText),
        createdAtEpochMs = date,
        mediaType = MediaType.valueOf(mediaType),
        seasonNumber = seasonNumber?.let(::SeasonNumber)
    )

    private fun toReview(reviewTitle: String?, reviewText: String?): Review? {
        val title = reviewTitle?.takeIf { it.isNotBlank() }?.let(::ReviewTitle)
        val text = reviewText?.takeIf { it.isNotBlank() }?.let(::ReviewText)
        return if (title == null && text == null) null else Review(title, text)
    }
}
