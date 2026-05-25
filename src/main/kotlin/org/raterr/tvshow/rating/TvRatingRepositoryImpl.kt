package org.raterr.tvshow.rating

import org.raterr.Score
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
        private val tvShowRepository: TvShowRepository,
        private val userRepository: UserRepository,
    ) : TvRatingRepository {

    override fun findById(id: TvRating.Id): TvRatingView? =
        id.value.let(tvRatingDAO::findById).getOrNull()?.toDomain()

    override fun findFirstByTvShowId(tvShowId: TvShow.Id): TvRatingView? =
        tvShowId.value.let(tvRatingDAO::findFirstByTvShowId).getOrNull()?.toDomain()

    override fun findByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): List<TvRatingView> =
        tvRatingDAO.findByTvShowIdAndUserId(tvShowId.value, userId.value).map { it.toDomain() }

    override fun findByUserId(userId: User.Id): List<TvRatingView> =
        tvRatingDAO.findByUserId(userId.value).map { it.toDomain() }

    override fun findAllWithoutUser(): List<TvRatingView> =
        tvRatingDAO.findAll().filter { it.userId == 0L }.map { it.toDomain() }

    override fun save(rating: TvRating) {
        rating.toEntity().let(tvRatingDAO::save)
    }

    override fun deleteByTvShowIdAndUserId(tvShowId: TvShow.Id, userId: User.Id): Int {
        val ratings = tvRatingDAO.findByTvShowIdAndUserId(tvShowId.value, userId.value)
        return ratings.filter { tvRatingDAO.delete(it); true }.size
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id,
        category: String?,
        limit: Int,
        name: String?
    ): List<TvRatingView> =
        tvRatingDAO.findByUserId(userId.value).map { it.toDomain() }.sortedByDescending { it.score }.take(limit)

    override fun findByUserIdOrderedByRank(userId: User.Id): List<TvRatingView> =
        tvRatingDAO.findByUserIdOrderByRank(userId.value).map { it.toDomain() }

    override fun updateRank(id: TvRating.Id, rank: TvRating.Rank): Int {
        val entity = tvRatingDAO.findById(id.value).getOrNull() ?: return 0
        tvRatingDAO.save(entity.copy(rank = rank.value))
        return 1
    }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<TvRatingView> {
        val sinceEpochMs = since.toEpochMilli()
        val userIdValues = userIds.map(User.Id::value)
        return tvRatingDAO.findAll()
            .filter { it.userId in userIdValues && it.createdAtEpochMs >= sinceEpochMs }
            .sortedByDescending { it.createdAtEpochMs }
            .map { it.toDomain() }
    }

    private fun TvRatingEntity.toDomain(): TvRatingView {
        val tvShow = tvShowId.let(TvShow::Id).let(tvShowRepository::findById)
        val user = userId.let(User::Id).let(userRepository::findById)

        return TvRatingView(
            id = id?.let { TvRating.Id(it) },
            tvShow = tvShow!!,
            user = user!!,
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            rank = TvRating.Rank(rank)
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
