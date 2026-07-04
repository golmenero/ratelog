package org.ratelog.user.profile

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.Username
import org.ratelog.feed.FeedItem
import org.ratelog.feed.FeedRepository
import org.ratelog.toDateString
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class GetProfile(
    val loggedUserId: User.Id,
    val userId: User.Id,
    val limit: Int,
)

data class Profile(
    val userId: User.Id,
    val username: Username,
    val email: Email,
    val memberSince: String,
    val lang: Lang,
    val isFollowed: Boolean,
    val feed: List<ProfileRating>,
    val hasMore: Boolean,
)

data class ProfileRating(
    val title: String,
    val tmdbId: Int,
    val mediaType: String,
    val seasonNumber: Int?,
    val score: Double,
    val reviewText: String?,
    val ratedAt: String,
    val createdAtEpochMs: Long,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
) {

    @Transactional
    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val user = userRepository.findById(query.userId) ?: raise(ProfileHandlerError.UserNotFound)
        val feed = feedRepository.findAll(listOf(query.userId), query.limit).map { toResponse(it) }
        val totalCount = feedRepository.count(listOf(query.userId))

        Profile(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            memberSince = user.createdAtEpochMs.toDateString(),
            lang = user.lang,
            isFollowed = userRepository.isFollowing(query.loggedUserId, query.userId),
            feed = feed,
            hasMore = totalCount > query.limit,
        )
    }

    private fun toResponse(rating: FeedItem) = ProfileRating(
        title = rating.title.value,
        tmdbId = rating.tmdbId.value,
        mediaType = MediaType.tvshow.name,
        seasonNumber = rating.seasonNumber?.value,
        score = rating.score?.value ?: 0.0,
        reviewText = rating.text,
        ratedAt = rating.createdAtEpochMs.toDateString(),
        createdAtEpochMs = rating.createdAtEpochMs,
    )
}
