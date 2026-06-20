package org.ratelog.tvshow.rating

import org.ratelog.Review
import org.ratelog.Rank
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import org.springframework.stereotype.Repository
import java.time.Instant

data class TvRating(
    val id: Id?,
    val tvShowId: TvShow.Id,
    val seasonRatings: List<SeasonRating>,
    val userId: User.Id,
    val createdAt: Instant,
    val score: Score? = null,
) {
    data class Id(val value: Long)

    fun deleteSeasonRating(seasonNumber: SeasonNumber) =
        copy(seasonRatings = seasonRatings - (seasonRatings.filter { it.seasonNumber == seasonNumber }).toSet())
            .updateScore()

    fun addSeasonRating(
        seasonNumber: SeasonNumber,
        directing: Score,
        cinematography: Score,
        acting: Score,
        soundtrack: Score,
        screenplay: Score,
        createdAt: Instant,
        review: Review?,
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
            score = Score((directing.value + cinematography.value + acting.value + soundtrack.value + screenplay.value) / 5.0),
            createdAt = createdAt,
            review = review,
        )).updateScore()

    private fun updateScore() = if (seasonRatings.isEmpty()) copy(score = null)
        else copy(score = seasonRatings.map { it.score.value }.average().let(::Score))

    companion object {
        fun create(tvShowId: TvShow.Id, userId: User.Id, now: Instant) = TvRating(
            id = null,
            tvShowId = tvShowId,
            seasonRatings = emptyList(),
            userId = userId,
            createdAt = now,
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
    val score: Score,
    val createdAt: Instant,
    val review: Review? = null,
) {
    data class Id(val value: Long)
}

@Repository
interface TvRatingRepository {
    fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): TvRating?
    fun findRankedByUserIdWithFilters(userId: User.Id, category: String?, limit: Int, name: String?): List<Pair<Rank, TvRating>>
    fun findFeedItemsByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant, limit: Int, offset: Int): List<FeedTvRow>

    fun save(rating: TvRating)
    fun deleteById(tvRatingId: TvRating.Id)
}
