package org.ratelog.tvshow.rating.addseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.Review
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import java.time.Instant
import org.springframework.transaction.annotation.Transactional

data class AddSeasonRating(
    val tvShowId: TvShow.Id,
    val seasonNumber: SeasonNumber,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
    val reviewTitle: String?,
    val reviewText: String?,
)

@Component
class AddSeasonRatingHandler(
    private val tvRatingRepository: TvRatingRepository,
) {
    @Transactional
    fun handle(command: AddSeasonRating): Either<AddSeasonRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { score ->
            ensure(score.value in 0.0..10.0) { AddSeasonRatingHandlerError.InvalidRatingValue }
        }

        val tvRating = tvRatingRepository.findByTvShowIdAndUserId(command.tvShowId, command.userId) ?: TvRating.create(command.tvShowId, command.userId, Instant.now())

        ensure(tvRating.seasonRatings.none { it.seasonNumber == command.seasonNumber }) {
            AddSeasonRatingHandlerError.RatingAlreadyExists
        }

        val review = parseReview(command.reviewTitle, command.reviewText).bind()

        tvRating.addSeasonRating(
            seasonNumber = command.seasonNumber,
            directing = command.directing,
            cinematography = command.cinematography,
            acting = command.acting,
            soundtrack = command.soundtrack,
            screenplay = command.screenplay,
            createdAt = Instant.now(),
            review = review,
        ).let(tvRatingRepository::save)
    }

    private fun parseReview(title: String?, text: String?): Either<AddSeasonRatingHandlerError, Review?> =
        Review.parse(title, text)
            .mapLeft { error ->
                when (error) {
                    org.ratelog.ParseError.InvalidReviewTitle -> AddSeasonRatingHandlerError.InvalidReviewTitle
                    org.ratelog.ParseError.InvalidReviewText -> AddSeasonRatingHandlerError.InvalidReviewText
                    else -> AddSeasonRatingHandlerError.InvalidReviewText
                }
            }
}
