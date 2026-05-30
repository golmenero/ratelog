package org.ratelog.tvshow.rating

import org.ratelog.Rank
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Repository
class TvRatingRepositoryImpl(
        private val tvRatingDAO: TvRatingDAO,
        private val seasonRatingDAO: SeasonRatingDAO,
    ) : TvRatingRepository {

    override fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): TvRating? =
        tvRatingDAO.findFirstByTvShowIdAndUserId(tvShowId.value, userId.value).getOrNull()?.toDomain()

    override fun save(rating: TvRating) {
        val savedEntity = tvRatingDAO.save(rating.toEntity())

        seasonRatingDAO.findByTvShowIdAndUserId(savedEntity.tvShowId, savedEntity.userId)
            .forEach { seasonRatingDAO.deleteById(it.id!!) }

        seasonRatingDAO.saveAll(rating.seasonRatings.map { it.toEntity() })
    }

    override fun deleteById(tvRatingId: TvRating.Id) {
        tvRatingId.value.let(tvRatingDAO::deleteById)
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<Pair<Rank, TvRating>> {
        val ranking = tvRatingDAO.findRanking(userId.value)
            .mapIndexed { index, id -> id to Rank(index + 1) }.toMap()

        return tvRatingDAO.findRankedByUserIdWithFilters(userId.value, category, name, limit)
            .map { ranking[it.id]!! to it.toDomain() }
    }

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
            score = score?.let(::Score),
        )
    }

    private fun TvRating.toEntity(): TvRatingEntity {
        return TvRatingEntity(
            id = id?.value,
            tvShowId = tvShowId.value,
            userId = userId.value,
            createdAtEpochMs = createdAt.toEpochMilli(),
            score = score?.value,
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
            id = null,
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
