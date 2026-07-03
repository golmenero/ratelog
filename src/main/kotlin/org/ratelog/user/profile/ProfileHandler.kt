package org.ratelog.user.profile

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import org.ratelog.Email
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.Username
import org.ratelog.toDateString
import org.ratelog.movie.rating.FeedMovieRow
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tvshow.rating.FeedTvRow
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.ratelog.user.UserRepository
import org.ratelog.feed.community.FollowedUserResult
import org.ratelog.feed.community.FollowedUsersHandler
import org.ratelog.feed.community.FollowedUsersQuery
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
    val movieRatings: List<ProfileRating>,
    val tvRatings: List<ProfileRating>,
    val movieHasMore: Boolean,
    val tvHasMore: Boolean,
    val followedUsers: List<FollowedUserResult>,
)

data class ProfileRating(
    val title: String,
    val tmdbId: Int,
    val type: String,
    val seasonNumber: Int?,
    val score: Double,
    val reviewText: String?,
    val ratedAt: String,
    val createdAtEpochMs: Long,
)

@Service
class ProfileHandler(
    private val userRepository: UserRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val followedUsersHandler: FollowedUsersHandler,
) {

    @Transactional
    fun handle(query: GetProfile): Either<ProfileHandlerError, Profile> = either {
        val user = userRepository.findById(query.userId) ?: raise(ProfileHandlerError.UserNotFound)
        val movieRatings = ratingRepository.findFeedItemsByUserIds(listOf(query.userId), query.limit).map { toResponse(it) }
        val tvRatings = tvRatingRepository.findFeedItemsByUserIds(listOf(query.userId), query.limit).map { toResponse(it) }

        val movieTotalCount = ratingRepository.countFeedItemsByUserIds(listOf(query.userId))
        val tvTotalCount = tvRatingRepository.countFeedItemsByUserIds(listOf(query.userId))

        val followedUsers = if (query.loggedUserId == query.userId) {
            followedUsersHandler.handle(FollowedUsersQuery(query.loggedUserId)).getOrElse { emptyList() }
        } else emptyList()

        Profile(
            userId = user.id!!,
            username = user.username,
            email = user.email,
            memberSince = user.createdAtEpochMs.toDateString(),
            lang = user.lang,
            isFollowed = userRepository.isFollowing(query.loggedUserId, query.userId),
            movieRatings = movieRatings.sortedByDescending { it.createdAtEpochMs },
            tvRatings = tvRatings.sortedByDescending { it.createdAtEpochMs },
            movieHasMore = movieTotalCount > query.limit,
            tvHasMore = tvTotalCount > query.limit,
            followedUsers = followedUsers,
        )
    }

    private fun toResponse(rating: FeedMovieRow) = ProfileRating(
        title = rating.title,
        tmdbId = rating.tmdbId,
        type = MediaType.movie.name,
        seasonNumber = null,
        score = rating.score ?: 0.0,
        reviewText = rating.reviewText,
        ratedAt = rating.createdAtEpochMs.toDateString(),
        createdAtEpochMs = rating.createdAtEpochMs,
    )

    private fun toResponse(rating: FeedTvRow) = ProfileRating(
        title = rating.title,
        tmdbId = rating.tmdbId,
        type = MediaType.tvshow.name,
        seasonNumber = rating.seasonNumber,
        score = rating.score ?: 0.0,
        reviewText = rating.reviewText,
        ratedAt = rating.createdAtEpochMs.toDateString(),
        createdAtEpochMs = rating.createdAtEpochMs,
    )
}
