package org.raterr.rating.add

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.follow.FollowRepository
import org.raterr.movie.get.GetMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.rating.Rating
import org.raterr.rating.RatingRankService
import org.raterr.rating.RatingRepository
import org.raterr.TmdbId
import org.raterr.UserId
import org.springframework.stereotype.Component

data class AddRating(
    val tmdbId: TmdbId,
    val userId: UserId,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
)

@Component
class AddRatingHandler(
    private val getMovieHandler: GetMovieHandler,
    private val ratingRepository: RatingRepository,
    private val followRepository: FollowRepository,
    private val ratingRankService: RatingRankService,
) {
    fun handle(command: AddRating): Either<AddRatingHandlerError, Unit> = either {
        listOf(
            "directing" to command.directing,
            "cinematography" to command.cinematography,
            "acting" to command.acting,
            "soundtrack" to command.soundtrack,
            "screenplay" to command.screenplay
        ).forEach { (field, value) ->
            ensure(value in 1.0..10.0) { AddRatingHandlerError.InvalidRatingValue }
        }

        val movie = GetMovie(TmdbId(command.tmdbId.value))
            .let(getMovieHandler::handle)
            .mapLeft { AddRatingHandlerError.MovieNotFound }
            .bind()

        val existingRating = ratingRepository.findByMovieIdAndUserId(movie.id!!, command.userId.value).firstOrNull()
        ensure(existingRating == null) { AddRatingHandlerError.RatingAlreadyExists }

        Rating(
            id = null,
            movieId = movie.id,
            userId = command.userId.value,
            directing = command.directing,
            cinematography = command.cinematography,
            acting = command.acting,
            soundtrack = command.soundtrack,
            screenplay = command.screenplay,
            createdAtEpochMs = System.currentTimeMillis()
        ).let(ratingRepository::save)

        ratingRankService.recalculateRanks(command.userId.value)

        followRepository.findByUserIdAndContentTypeAndContentTmdbId(
            command.userId.value,
            "movie",
            command.tmdbId.value
        ).ifPresent(followRepository::delete)
    }
}
