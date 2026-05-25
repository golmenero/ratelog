package org.raterr.tvshow.rating

import org.raterr.Score
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

data class TvRatingView(
    val id: TvRating.Id?,
    val tvShow: TvShow,
    val user: User,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
    val rank: TvRating.Rank,
) {
    val score = (directing.value + cinematography.value + acting.value + soundtrack.value + screenplay.value) / 5.0
}

@Repository
interface TvRatingRepository {
    fun findById(id: TvRating.Id): TvRatingView?
    fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRatingView?
    fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): List<TvRatingView>
    fun findByUserId(userId: User.Id): List<TvRatingView>
    fun findAllWithoutUser(): List<TvRatingView>
    fun findRankedByUserIdWithFilters(userId: User.Id, category: String?, limit: Int, name: String?): List<TvRatingView>
    fun findByUserIdOrderedByRank(userId: User.Id): List<TvRatingView>
    fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRatingView>

    fun save(rating: TvRating)
    fun deleteByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): Int
    fun updateRank(id: TvRating.Id, rank: TvRating.Rank): Int
}
