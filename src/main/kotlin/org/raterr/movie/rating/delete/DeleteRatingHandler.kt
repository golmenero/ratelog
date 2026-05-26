package org.raterr.movie.rating.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.raterr.movie.rating.RatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

data class DeleteRating(
    val movieId: Movie.Id,
    val userId: User.Id,
)

@Component
class DeleteRatingHandler(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(command: DeleteRating): Either<DeleteRatingHandlerError, Unit> = either {
        val movie = movieRepository.findById(command.movieId)

        ensure(movie != null && movie.id != null) { DeleteRatingHandlerError.MovieNotFound }

        val rating = ratingRepository.findFirstByMovieId(movie.id) ?:
        raise(DeleteRatingHandlerError.RatingNotFound)

        ratingRepository.deleteById(rating.id!!)
    }
}
