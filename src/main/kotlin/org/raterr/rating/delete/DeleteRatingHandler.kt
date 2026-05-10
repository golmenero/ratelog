package org.raterr.rating.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.movie.MovieRepository
import org.raterr.rating.RatingRepository
import org.springframework.stereotype.Component

@Component
class DeleteRatingHandler(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(command: DeleteRating): Either<DeleteRatingHandlerError, Unit> = either {
        val movie = movieRepository.findByTmdbId(command.tmdbId.value)
            .orElse(null)

        ensure(movie != null) { DeleteRatingHandlerError.MovieNotFound }

        val deletedCount = ratingRepository.deleteByMovieIdAndUserId(movie.id!!, command.userId.value)
        ensure(deletedCount > 0) { DeleteRatingHandlerError.RatingNotFound }
    }
}
