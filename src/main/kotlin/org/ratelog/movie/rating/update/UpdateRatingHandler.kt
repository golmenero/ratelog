package org.ratelog.movie.rating.update

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Review
import org.ratelog.Score
import org.ratelog.movie.Movie
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class UpdateRating(
    val movieId: Movie.Id,
    val userId: User.Id,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val review: String?,
)

@Component
class UpdateRatingHandler(
    private val ratingRepository: RatingRepository,
) {
    @Transactional
    fun handle(command: UpdateRating): Either<UpdateRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { value ->
            ensure(value in 0.0..10.0) { UpdateRatingHandlerError.InvalidRatingValue }
        }

        val rating = ratingRepository.findByMovieIdAndUserId(command.movieId, command.userId)
            ?: raise(UpdateRatingHandlerError.RatingNotFound)

        rating.copy(
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            review = command.review?.takeIf { it.isNotBlank() }?.let(Review::sanitize),
        ).updateScore().let(ratingRepository::save)
    }
}
