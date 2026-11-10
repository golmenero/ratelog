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
    val reviewTitle: String?,
    val reviewText: String?,
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

        val review = parseReview(command.reviewTitle, command.reviewText).bind()

        rating.copy(
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            review = review,
        ).updateScore().let(ratingRepository::save)
    }

    private fun parseReview(title: String?, text: String?): Either<UpdateRatingHandlerError, Review?> =
        Review.parse(title, text)
            .mapLeft { error ->
                when (error) {
                    org.ratelog.ParseError.InvalidReviewTitle -> UpdateRatingHandlerError.InvalidReviewTitle
                    org.ratelog.ParseError.InvalidReviewText -> UpdateRatingHandlerError.InvalidReviewText
                    else -> UpdateRatingHandlerError.InvalidReviewText
                }
            }
}
