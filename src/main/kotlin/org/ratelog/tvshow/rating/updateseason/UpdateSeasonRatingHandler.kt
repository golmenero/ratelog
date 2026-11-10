package org.ratelog.tvshow.rating.updateseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Review
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class UpdateSeasonRating(
    val tvShowId: TvShow.Id,
    val seasonNumber: SeasonNumber,
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
class UpdateSeasonRatingHandler(
    private val tvRatingRepository: TvRatingRepository,
) {
    @Transactional
    fun handle(command: UpdateSeasonRating): Either<UpdateSeasonRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { value ->
            ensure(value in 0.0..10.0) { UpdateSeasonRatingHandlerError.InvalidRatingValue }
        }

        val tvRating = tvRatingRepository.findByTvShowIdAndUserId(command.tvShowId, command.userId)
            ?: raise(UpdateSeasonRatingHandlerError.RatingNotFound)

        ensure(tvRating.seasonRatings.any { it.seasonNumber == command.seasonNumber }) {
            UpdateSeasonRatingHandlerError.SeasonRatingNotFound
        }

        val review = parseReview(command.reviewTitle, command.reviewText).bind()

        tvRating.updateSeasonRating(
            seasonNumber = command.seasonNumber,
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            review = review,
        ).let(tvRatingRepository::save)
    }

    private fun parseReview(title: String?, text: String?): Either<UpdateSeasonRatingHandlerError, Review?> =
        Review.parse(title, text)
            .mapLeft { error ->
                when (error) {
                    org.ratelog.ParseError.InvalidReviewTitle -> UpdateSeasonRatingHandlerError.InvalidReviewTitle
                    org.ratelog.ParseError.InvalidReviewText -> UpdateSeasonRatingHandlerError.InvalidReviewText
                    else -> UpdateSeasonRatingHandlerError.InvalidReviewText
                }
            }
}
