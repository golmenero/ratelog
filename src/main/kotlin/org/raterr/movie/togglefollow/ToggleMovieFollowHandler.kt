package org.raterr.movie.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

sealed interface ToggleMovieFollowHandlerError {
    data object MovieNotFound : ToggleMovieFollowHandlerError
}

data class ToggleMovieFollow(
    val movieId: Movie.Id,
    val userId: User.Id,
)

@Component
class ToggleMovieFollowHandler(
    private val movieRepository: MovieRepository,
) {
    fun handle(command: ToggleMovieFollow): Either<ToggleMovieFollowHandlerError, Unit> = either {
        val movie = movieRepository.findById(command.movieId) ?: raise(ToggleMovieFollowHandlerError.MovieNotFound)

        movie.toggleFollow(System.currentTimeMillis()).let(movieRepository::save)
    }
}
