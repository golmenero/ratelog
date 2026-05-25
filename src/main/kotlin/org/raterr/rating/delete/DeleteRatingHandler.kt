package org.raterr.rating.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.TmdbId
import org.raterr.movie.MovieRepository
import org.raterr.rating.rank.RankRatingHandler
import org.raterr.rating.RatingRepository
import org.raterr.rating.rank.RankRating
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
    private val rankRatingHandler: RankRatingHandler,
) {
    fun handle(command: DeleteRating): Either<DeleteRatingHandlerError, Unit> = either {
        val movie = movieRepository.findByTmdbId(command.tmdbId)

        ensure(movie != null && movie.id != null) { DeleteRatingHandlerError.MovieNotFound }

        val deletedCount = ratingRepository.deleteByMovieIdAndUserId(movie.id, command.userId)
        ensure(deletedCount > 0) { DeleteRatingHandlerError.RatingNotFound }

        command.userId.let(::RankRating).let(rankRatingHandler::handle)
    }
}
