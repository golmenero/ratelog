package org.raterr.movie.rating.add

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.Score
import org.raterr.TmdbId
import org.raterr.movie.MovieRepository
import org.raterr.movie.get.GetMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.movie.rating.Rating
import org.raterr.movie.rating.rank.RankRatingHandler
import org.raterr.movie.rating.RatingRepository
import org.raterr.movie.rating.rank.RankRating
import org.raterr.user.User
import org.springframework.stereotype.Component
import java.time.Instant

data class AddRating(
    val tmdbId: TmdbId,
    val userId: User.Id,
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
    private val movieRepository: MovieRepository,
    private val rankRatingHandler: RankRatingHandler,
) {
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

        val movie = GetMovie(TmdbId(command.tmdbId.value))
            .let(getMovieHandler::handle)
            .mapLeft { AddRatingHandlerError.MovieNotFound }
            .bind()

        val existingRating = ratingRepository.findByMovieIdAndUserId(movie.id!!, command.userId).firstOrNull()
        ensure(existingRating == null) { AddRatingHandlerError.RatingAlreadyExists }

        Rating(
            id = null,
            movieId = movie.id,
            userId = command.userId,
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            createdAt = Instant.now(),
            rank = Rating.Rank(0)
        ).let(ratingRepository::save)

        command.userId.let(::RankRating).let(rankRatingHandler::handle)

        if (movie.followed) {
            movie.toggleFollow(System.currentTimeMillis()).let(movieRepository::save)
        }
    }
}
