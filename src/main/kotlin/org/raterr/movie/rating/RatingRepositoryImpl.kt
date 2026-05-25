package org.raterr.movie.rating

import org.raterr.Rank
import org.raterr.Score
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.raterr.user.User
import org.raterr.user.UserRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Repository
class RatingRepositoryImpl(
        private val ratingDAO: RatingDAO,
    ) : RatingRepository {

    override fun findById(id: Rating.Id): Rating? =
        id.value.let(ratingDAO::findById).getOrNull()?.toDomain()

    override fun findFirstByMovieId(movieId: Movie.Id): Rating? =
        movieId.value.let(ratingDAO::findFirstByMovieId).getOrNull()?.toDomain()

    override fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): List<Rating> =
        ratingDAO.findByMovieIdAndUserId(movieId.value, userId.value).map { it.toDomain() }

    override fun findByUserId(userId: User.Id): List<Rating> =
        ratingDAO.findByUserId(userId.value).map { it.toDomain() }

    override fun findAllWithoutUser(): List<Rating> =
        ratingDAO.findAll().filter { it.userId == 0L }.map { it.toDomain() }

    override fun save(rating: Rating) {
        rating.toEntity().let(ratingDAO::save)
    }

    override fun deleteByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): Int {
        val ratings = ratingDAO.findByMovieIdAndUserId(movieId.value, userId.value)
        return ratings.filter { ratingDAO.delete(it); true }.size
    }

    override fun findRankedByUserIdWithFilters(
        userId: User.Id, category: String?, limit: Int, name: String?
    ): List<Rating> =
        ratingDAO.findByUserId(userId.value).map { it.toDomain() }.sortedByDescending { it.score.value }.take(limit)

    override fun findByUserIdOrderedByRank(userId: User.Id): List<Rating> =
        ratingDAO.findByUserIdOrderByRank(userId.value).map { it.toDomain() }

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
            score = score.let(::Score),
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
            score = score.value,
        )
    }
}
