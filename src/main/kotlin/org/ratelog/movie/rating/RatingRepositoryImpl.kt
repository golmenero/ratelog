package org.ratelog.movie.rating

import org.ratelog.Review
import org.ratelog.Rank
import org.ratelog.Score
import org.ratelog.movie.Movie
import org.ratelog.user.User
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class RatingRepositoryImpl(
        private val ratingDAO: RatingDAO,
    ) : RatingRepository {
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
    ): List<Pair<Rank, Rating>> =
        ratingDAO.findRankedRows(userId.value, category, name, limit)
            .map { Rank(it.rank.toInt()) to it.toDomain() }

    override fun findFeedItemsByUserIds(userIds: List<User.Id>, limit: Int): List<FeedMovieRow> {
        val userIdValues = userIds.map(User.Id::value)
        return ratingDAO.findFeedItemsByUserIds(userIdValues, limit)
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
            review = reviewText?.takeIf { it.isNotBlank() }?.let(::Review),
        )
    }

    private fun RatedMovieRow.toDomain(): Rating {
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
            review = reviewText?.takeIf { it.isNotBlank() }?.let(::Review),
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
            reviewText = review?.value,
        )
    }
}
