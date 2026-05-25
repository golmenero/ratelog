package org.raterr.tvrating

import org.raterr.Score
import org.raterr.Username
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import org.springframework.stereotype.Repository
import java.time.Instant

data class TvRating(
    val id: Id?,
    val tvShowId: TvShow.Id,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
    val rank: Rank,
) {
    data class Id(val value: Long)
    data class Rank(val value: Int)
}

data class TvRatingWithUsername(
    val id: TvRating.Id,
    val tvShowId: TvShow.Id,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
    val username: Username
)

@Repository
interface TvRatingRepository {
    fun findById(id: TvRating.Id): TvRating?
    fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRating?
    fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): List<TvRating>
    fun findByUserId(userId: User.Id): List<TvRating>
    fun findAllWithoutUser(): List<TvRating>
    fun save(rating: TvRating): TvRating
    fun deleteByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): Int
    fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRating>
    fun findByUserIdOrderedByRank(userId: User.Id): List<TvRating>
    fun updateRank(id: TvRating.Id, rank: TvRating.Rank): Int
    fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRatingWithUsername>
}
