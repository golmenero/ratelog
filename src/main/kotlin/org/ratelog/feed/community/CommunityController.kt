package org.ratelog.feed.community

import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.feed.FeedItem
import org.ratelog.toDateString
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class FeedResponse(
    val username: String,
    val title: String,
    val tmdbId: Int,
    val mediaType: String,
    val type: String,
    val seasonNumber: Int?,
    val score: Double,
    val reviewTitle: String?,
    val reviewText: String?,
    val reviewTextPreview: String?,
    val hasFullReview: Boolean,
    val ratedAt: String,
    val createdAtEpochMs: Long,
)

data class UserResponse(
    val id: Long,
    val username: String,
)

@Controller
class CommunityController(
    private val communityHandler: CommunityHandler,
) {
    @GetMapping("/community")
    fun communityPage(
        @CurrentUser user: User,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        model: Model
    ): String {
        FeedQuery(user.id!!, limit, user.metadataLang).let(communityHandler::handle)
            .fold(
                { },
                {
                    model.addAttribute("followedUsers", it.followedUsers.map(::toResponse))
                    model.addAttribute("feed", it.feed.map(::toResponse))
                    model.addAttribute("hasMore", it.hasMore)
                }
            )

        model.addAttribute("limit", limit)
        return "community"
    }

    private fun toResponse(row: FeedItem) = FeedResponse(
        username = row.username.value,
        title = row.title.value,
        tmdbId = row.tmdbId.value,
        mediaType = row.mediaType.name,
        type = row.mediaType.name,
        seasonNumber = row.seasonNumber?.value,
        score = row.score?.value ?: 0.0,
        reviewTitle = row.review?.title?.value,
        reviewText = row.review?.text?.value,
        reviewTextPreview = row.review?.text?.value?.let { if (it.length > 250) it.take(250) + "..." else it },
        hasFullReview = row.review?.text?.value?.let { it.length > 250 } ?: false,
        ratedAt = row.createdAtEpochMs.toDateString(),
        createdAtEpochMs = row.createdAtEpochMs,
    )

    private fun toResponse(user: User) = UserResponse(
        id = user.id!!.value,
        username = user.username.value,
    )
}
