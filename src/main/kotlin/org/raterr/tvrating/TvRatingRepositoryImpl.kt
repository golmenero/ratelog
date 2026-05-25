package org.raterr.tvrating

import org.raterr.Score
import org.raterr.Username
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Repository
class TvRatingRepositoryImpl(private val tvRatingDAO: TvRatingDAO) : TvRatingRepository {

    override fun findById(id: TvRating.Id): TvRating? =
        id.value.let(tvRatingDAO::findById).getOrNull()?.toDomain()

    override fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRating? =
        tvShowId.value.let(tvRatingDAO::findFirstByTvShowId).getOrNull()?.toDomain()

    override fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): List<TvRating> =
        tvRatingDAO.findByTvShowIdAndUserId(tvShowId.value, userId.value).map { it.toDomain() }

    override fun findByUserId(userId: User.Id): List<TvRating> =
        tvRatingDAO.findByUserId(userId.value).map { it.toDomain() }

    override fun findAllWithoutUser(): List<TvRating> =
        tvRatingDAO.findAll().filter { it.userId == 0L }.map { it.toDomain() }

    override fun save(rating: TvRating): TvRating =
        rating.toEntity().let(tvRatingDAO::save).toDomain()

    override fun deleteByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): Int {
        val ratings = tvRatingDAO.findByTvShowIdAndUserId(tvShowId.value, userId.value)
        return ratings.filter { tvRatingDAO.delete(it); true }.size
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRating> {
        val all = tvRatingDAO.findByUserId(userId.value)
            .map { it.toDomain() }
            .sortedByDescending { TvRatingScoreService.score(it) }

        return all.take(limit)
    }

    override fun findByUserIdOrderedByRank(userId: User.Id): List<TvRating> =
        tvRatingDAO.findByUserIdOrderByRank(userId.value).map { it.toDomain() }

    override fun updateRank(id: TvRating.Id, rank: TvRating.Rank): Int {
        val entity = tvRatingDAO.findById(id.value).getOrNull() ?: return 0
        tvRatingDAO.save(entity.copy(rank = rank.value))
        return 1
    }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRatingWithUsername> {
        val sinceEpochMs = since.toEpochMilli()
        val userIdValues = userIds.map(User.Id::value)
        return tvRatingDAO.findAll()
            .filter { it.userId in userIdValues && it.createdAtEpochMs >= sinceEpochMs }
            .sortedByDescending { it.createdAtEpochMs }
            .map { it.toTvRatingWithUsernameEntity().toDomainWithUsername() }
    }

    private fun TvRatingEntity.toDomain(): TvRating {
        return TvRating(
            id = id?.let { TvRating.Id(it) },
            tvShowId = TvShow.Id(tvShowId),
            userId = User.Id(userId),
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            rank = TvRating.Rank(rank)
        )
    }

    private fun TvRatingEntity.toDomainWithUsername(): TvRatingWithUsername {
        return TvRatingWithUsername(
            id = TvRating.Id(id!!),
            tvShowId = TvShow.Id(tvShowId),
            userId = User.Id(userId),
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            username = Username("")
        )
    }

    private fun TvRatingEntity.toTvRatingWithUsernameEntity(): TvRatingWithUsernameEntity {
        return TvRatingWithUsernameEntity(
            id = id,
            tvShowId = tvShowId,
            userId = userId,
            directing = directing,
            cinematography = cinematography,
            acting = acting,
            soundtrack = soundtrack,
            screenplay = screenplay,
            createdAtEpochMs = createdAtEpochMs,
            username = ""
        )
    }

    private fun TvRatingWithUsernameEntity.toDomainWithUsername(): TvRatingWithUsername {
        return TvRatingWithUsername(
            id = TvRating.Id(id!!),
            tvShowId = TvShow.Id(tvShowId),
            userId = User.Id(userId),
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            username = Username(username)
        )
    }

    private fun TvRating.toEntity(): TvRatingEntity {
        return TvRatingEntity(
            id = id?.value,
            tvShowId = tvShowId.value,
            userId = userId.value,
            directing = directing.value,
            cinematography = cinematography.value,
            acting = acting.value,
            soundtrack = soundtrack.value,
            screenplay = screenplay.value,
            createdAtEpochMs = createdAt.toEpochMilli(),
            rank = rank.value
        )
    }
}
