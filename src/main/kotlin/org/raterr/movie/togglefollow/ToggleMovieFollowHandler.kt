package org.raterr.movie.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.TmdbId
import org.raterr.movie.MovieRepository
import org.raterr.movie.get.GetMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.user.User
import org.springframework.stereotype.Component

sealed interface ToggleMovieFollowHandlerError {
    data object MovieNotFound : ToggleMovieFollowHandlerError
}

data class ToggleMovieFollow(
    val tmdbId: TmdbId,
    val userId: User.Id,
)

@Component
class ToggleMovieFollowHandler(
    private val getMovieHandler: GetMovieHandler,
    private val movieRepository: MovieRepository,
) {
    fun handle(command: ToggleMovieFollow): Either<ToggleMovieFollowHandlerError, Unit> = either {
        val movie = GetMovie(tmdbId = command.tmdbId)
            .let(getMovieHandler::handle)
            .mapLeft { ToggleMovieFollowHandlerError.MovieNotFound }
            .bind()

        movie.toggleFollow(System.currentTimeMillis()).let(movieRepository::save)
    }
}
