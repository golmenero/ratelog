package org.raterr.tvshow.rating

import org.raterr.Rank
import org.raterr.Score
import org.raterr.SeasonNumber
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Repository
class TvRatingRepositoryImpl(
        private val tvRatingDAO: TvRatingDAO,
        private val seasonRatingDAO: SeasonRatingDAO,
    ) : TvRatingRepository {

    override fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRating? =
        tvShowId.value.let(tvRatingDAO::findFirstByTvShowId).getOrNull()?.toDomain()

    override fun save(rating: TvRating) {
        val savedEntity = tvRatingDAO.save(rating.toEntity())

        seasonRatingDAO.findByTvShowIdAndUserId(savedEntity.tvShowId, savedEntity.userId)
            .forEach { seasonRatingDAO.deleteById(it.id!!) }

        rating.seasonRatings
            .map { it.toEntity() }
            .forEach(seasonRatingDAO::save)
    }

    override fun deleteById(tvRatingId: TvRating.Id) {
        tvRatingId.value.let(tvRatingDAO::deleteById)
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRating> =
        tvRatingDAO.findByUserId(userId.value).map { it.toDomain() }.sortedByDescending { it.score.value }.take(limit)

    override fun findByUserIdOrderedByRank(userId: User.Id): List<TvRating> =
        tvRatingDAO.findByUserIdOrderByScore(userId.value).map { it.toDomain() }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRating> {
        val sinceEpochMs = since.toEpochMilli()
        val userIdValues = userIds.map(User.Id::value)
        return tvRatingDAO.findAll()
            .filter { it.userId in userIdValues && it.createdAtEpochMs >= sinceEpochMs }
            .sortedByDescending { it.createdAtEpochMs }
            .map { it.toDomain() }
    }

    private fun TvRatingEntity.toDomain(): TvRating {
        val seasonRatings = seasonRatingDAO.findByTvShowIdAndUserId(tvShowId, userId).map { it.toDomain() }
        return TvRating(
            id = id?.let { TvRating.Id(it) },
            tvShowId = tvShowId.let(TvShow::Id),
            userId = userId.let(User::Id),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            seasonRatings = seasonRatings,
            score = score.let(::Score),
        )
    }

    private fun TvRating.toEntity(): TvRatingEntity {
        return TvRatingEntity(
            id = id?.value,
            tvShowId = tvShowId.value,
            userId = userId.value,
            createdAtEpochMs = createdAt.toEpochMilli(),
            score = score.value,
        )
    }

    private fun SeasonRatingEntity.toDomain(): SeasonRating {
        return SeasonRating(
            id = id?.let { SeasonRating.Id(it) },
            tvShowId = tvShowId.let(TvShow::Id),
            seasonNumber = SeasonNumber(seasonNumber),
            userId = userId.let(User::Id),
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
        )
    }

    private fun SeasonRating.toEntity(): SeasonRatingEntity {
        return SeasonRatingEntity(
            id = id?.value,
            tvShowId = tvShowId.value,
            seasonNumber = seasonNumber.value,
            userId = userId.value,
            directing = directing.value,
            cinematography = cinematography.value,
            acting = acting.value,
            soundtrack = soundtrack.value,
            screenplay = screenplay.value,
            createdAtEpochMs = createdAt.toEpochMilli(),
        )
    }
}
