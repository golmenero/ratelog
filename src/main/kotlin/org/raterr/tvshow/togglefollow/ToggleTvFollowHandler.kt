package org.raterr.tvshow.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.TmdbId
import org.raterr.movie.get.GetMovie
import org.raterr.movie.get.GetMovieHandler
import org.raterr.movie.togglefollow.ToggleMovieFollowHandlerError
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.get.GetTvShow
import org.raterr.tvshow.get.GetTvShowHandler
import org.raterr.user.User
import org.springframework.stereotype.Component

sealed interface ToggleTvFollowHandlerError {
    data object TvShowNotFound : ToggleTvFollowHandlerError
}

data class ToggleTvFollow(
    val tmdbId: TmdbId,
    val userId: User.Id,
)

@Component
class ToggleTvFollowHandler(
    private val getTvShowHandler: GetTvShowHandler,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(command: ToggleTvFollow): Either<ToggleTvFollowHandlerError, Unit> = either {
        val show = GetTvShow(tmdbId = command.tmdbId)
            .let(getTvShowHandler::handle)
            .mapLeft { ToggleTvFollowHandlerError.TvShowNotFound }
            .bind()

        show.toggleFollow(System.currentTimeMillis()).let(tvShowRepository::save)
    }
}
