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
        private val movieRepository: MovieRepository,
        private val userRepository: UserRepository,
    ) : RatingRepository {

    override fun findById(id: Rating.Id): RatingView? =
        id.value.let(ratingDAO::findById).getOrNull()?.toDomain()

    override fun findFirstByMovieId(movieId: Movie.Id): RatingView? =
        movieId.value.let(ratingDAO::findFirstByMovieId).getOrNull()?.toDomain()

    override fun findByMovieIdAndUserId(movieId: Movie.Id, userId: User.Id): List<RatingView> =
        ratingDAO.findByMovieIdAndUserId(movieId.value, userId.value).map { it.toDomain() }

    override fun findByUserId(userId: User.Id): List<RatingView> =
        ratingDAO.findByUserId(userId.value).map { it.toDomain() }

    override fun findAllWithoutUser(): List<RatingView> =
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
    ): List<RatingView> =
        ratingDAO.findByUserId(userId.value).map { it.toDomain() }.sortedByDescending { it.score }.take(limit)

    override fun findByUserIdOrderedByRank(userId: User.Id): List<RatingView> =
        ratingDAO.findByUserIdOrderByRank(userId.value).map { it.toDomain() }

    override fun updateRank(id: Rating.Id, rank: Rank): Int {
        val entity = ratingDAO.findById(id.value).getOrNull() ?: return 0
        ratingDAO.save(entity.copy(rank = rank.value))
        return 1
    }

    override fun findByUserIdsAndLastDays(userIds: List<User.Id>, since: Instant): List<RatingView> {
        val sinceEpochMs = since.toEpochMilli()
        val userIdValues = userIds.map(User.Id::value)
        return ratingDAO.findAll()
            .filter { it.userId in userIdValues && it.createdAtEpochMs >= sinceEpochMs }
            .sortedByDescending { it.createdAtEpochMs }
            .map { it.toDomain() }
    }

    private fun RatingEntity.toDomain(): RatingView {
        val movie = movieId.let(Movie::Id).let(movieRepository::findById)
        val user = userId.let(User::Id).let(userRepository::findById)

        return RatingView(
            id = id?.let { Rating.Id(it) },
            movie = movie!!,
            user = user!!,
            directing = Score(directing),
            cinematography = Score(cinematography),
            acting = Score(acting),
            soundtrack = Score(soundtrack),
            screenplay = Score(screenplay),
            createdAt = Instant.ofEpochMilli(createdAtEpochMs),
            rank = Rank(rank)
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
            rank = rank.value
        )
    }
}
