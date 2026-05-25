package org.raterr.rating.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.TmdbId
import org.raterr.movie.MovieRepository
import org.raterr.rating.RatingRankService
import org.raterr.rating.RatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

data class DeleteRating(
    val tmdbId: TmdbId,
    val userId: User.Id,
)

@Component
class DeleteRatingHandler(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val ratingRankService: RatingRankService,
) {
    fun handle(command: DeleteRating): Either<DeleteRatingHandlerError, Unit> = either {
        val movie = movieRepository.findByTmdbId(command.tmdbId)

        ensure(movie != null) { DeleteRatingHandlerError.MovieNotFound }

        val deletedCount = ratingRepository.deleteByMovieIdAndUserId(movie.id!!.value, command.userId.value)
        ensure(deletedCount > 0) { DeleteRatingHandlerError.RatingNotFound }

        ratingRankService.recalculateRanks(command.userId.value)
    }
}
