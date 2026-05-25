package org.raterr.tvshow.rating

import org.raterr.Rank
import org.raterr.Score
import org.raterr.SeasonNumber
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import org.springframework.stereotype.Repository
import java.time.Instant

data class TvRating(
    val id: Id?,
    val tvShowId: TvShow.Id,
    val seasonRatings: List<SeasonRating>,
    val userId: User.Id,
    val createdAt: Instant,
    val rank: Rank,
) {
    data class Id(val value: Long)

    val score = seasonRatings.map { it.score }.average()

    fun deleteSeasonRating(seasonNumber: SeasonNumber) =
        copy(seasonRatings = seasonRatings - (seasonRatings.filter { it.seasonNumber == seasonNumber }).toSet())

    fun addSeasonRating(
        seasonNumber: SeasonNumber,
        directing: Score,
        cinematography: Score,
        acting: Score,
        soundtrack: Score,
        screenplay: Score,
        createdAt: Instant,
    ) =
        copy(seasonRatings = seasonRatings + SeasonRating(
            id = null,
            tvShowId = tvShowId,
            seasonNumber = seasonNumber,
            userId = userId,
            directing = directing,
            cinematography = cinematography,
            acting = acting,
            soundtrack = soundtrack,
            screenplay = screenplay,
            createdAt = createdAt
        ))

    companion object {
        fun create(tvShowId: TvShow.Id, userId: User.Id, now: Instant) = TvRating(
            id = null,
            tvShowId = tvShowId,
            seasonRatings = emptyList(),
            userId = userId,
            createdAt = now,
            rank = Rank(0)
        )
    }
}

data class SeasonRating(
    val id: Id?,
    val tvShowId: TvShow.Id,
    val seasonNumber: SeasonNumber,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val createdAt: Instant,
) {
    data class Id(val value: Long)

    val score = (directing.value + cinematography.value + acting.value + soundtrack.value + screenplay.value) / 5.0
}

@Repository
interface TvRatingRepository {
    fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRating?
    fun findRankedByUserIdWithFilters(userId: User.Id, category: String?, limit: Int, name: String?): List<TvRating>
    fun findByUserIdOrderedByRank(userId: User.Id): List<TvRating>
    fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRating>

    fun save(rating: TvRating)
    fun deleteById(tvRatingId: TvRating.Id)
    fun updateRank(id: TvRating.Id, rank: Rank): Int
}
