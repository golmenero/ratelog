package org.raterr.tvshow.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.TmdbId
import org.raterr.tvshow.TvShowRepository
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
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(command: ToggleTvFollow): Either<ToggleTvFollowHandlerError, Unit> = either {
        val show = command.tmdbId.let(tvShowRepository::findByTmdbId)
            ?: raise(ToggleTvFollowHandlerError.TvShowNotFound)

        show.toggleFollow(System.currentTimeMillis()).let(tvShowRepository::save)
    }
}
