package org.raterr.tvshow.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.TmdbId
import org.raterr.movie.togglefollow.ToggleMovieFollowHandlerError
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.detail.GetTvShowDetail
import org.raterr.tvshow.detail.DetailTvShowHandler
import org.raterr.user.User
import org.springframework.stereotype.Component

sealed interface ToggleTvFollowHandlerError {
    data object TvShowNotFound : ToggleTvFollowHandlerError
}

data class ToggleTvFollow(
    val tvShowId: TvShow.Id,
    val userId: User.Id,
)

@Component
class ToggleTvFollowHandler(
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(command: ToggleTvFollow): Either<ToggleTvFollowHandlerError, Unit> = either {
        val show = tvShowRepository.findById(command.tvShowId) ?: raise(ToggleTvFollowHandlerError.TvShowNotFound)

        show.toggleFollow(System.currentTimeMillis()).let(tvShowRepository::save)
    }
}
