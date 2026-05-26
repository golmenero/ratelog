package org.raterr.movie.rating

import org.raterr.Rank
import org.raterr.Score
import org.raterr.movie.Movie
import org.raterr.user.User
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Repository
class RatingRepositoryImpl(
        private val ratingDAO: RatingDAO,
    ) : RatingRepository {
    override fun findFirstByMovieId(movieId: Movie.Id): Rating? =
        movieId.value.let(ratingDAO::findFirstByMovieId).getOrNull()?.toDomain()

    override fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Rating? =
        ratingDAO.findFirstByMovieIdAndUserId(movieId.value, userId.value)?.toDomain()

    override fun save(rating: Rating) {
        rating.toEntity().let(ratingDAO::save)
    }

    override fun deleteById(ratingId: Rating.Id) {
        ratingDAO.deleteById(ratingId.value)
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id, category: String?, limit: Int, name: String?
    ): List<Pair<Rank, Rating>> {
        val ranking = ratingDAO.findRanking(userId.value)
            .mapIndexed { index, id -> id to Rank(index + 1) }.toMap()

        return ratingDAO.findRankedByUserIdWithFilters(userId.value, category, name, limit)
            .map { ranking[it.id]!! to it.toDomain() }
    }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<Rating> {
        val sinceEpochMs = since.toEpochMilli()
        val userIdValues = userIds.map(User.Id::value)
        return ratingDAO.findAll()
            .filter { it.userId in userIdValues && it.createdAtEpochMs >= sinceEpochMs }
            .sortedByDescending { it.createdAtEpochMs }
            .map { it.toDomain() }
    }

    private fun RatingEntity.toDomain(): Rating {
        return Rating(
            id = id?.let { Rating.Id(it) },
            movieId = movieId.let(Movie::Id),
            userId = userId.let(User::Id),
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            score = score?.let(::Score),
        )
    }

    private fun Rating.toEntity(): RatingEntity {
        return RatingEntity(
            id = id?.value,
            movieId = movieId.value,
            userId = userId.value,
            directing = directing.value,
            cinematography = cinematography.value,
            acting = acting.value,
            soundtrack = soundtrack.value,
            screenplay = screenplay.value,
            createdAtEpochMs = createdAt.toEpochMilli(),
            score = score?.value,
        )
    }
}
