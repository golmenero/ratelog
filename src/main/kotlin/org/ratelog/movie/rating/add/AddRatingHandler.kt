package org.ratelog.movie.rating.add

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Review
import org.ratelog.Score
import org.ratelog.movie.Movie
import org.ratelog.movie.rating.Rating
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import java.time.Instant
import org.springframework.transaction.annotation.Transactional

data class AddRating(
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
class AddRatingHandler(
    private val ratingRepository: RatingRepository,
) {
    @Transactional
    fun handle(command: AddRating): Either<AddRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { value ->
            ensure(value in 1.0..10.0) { AddRatingHandlerError.InvalidRatingValue }
        }

        val existingRating = ratingRepository.findByMovieIdAndUserId(command.movieId, command.userId)
        ensure(existingRating == null) { AddRatingHandlerError.RatingAlreadyExists }

        Rating(
            id = null,
            movieId = command.movieId,
            userId = command.userId,
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            createdAt = Instant.now(),
            review = command.review?.takeIf { it.isNotBlank() }?.let(Review::sanitize),
        ).updateScore().let(ratingRepository::save)
    }
}
