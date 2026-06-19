package org.ratelog.tvshow.togglefollow

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

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
    @Transactional
    fun handle(command: ToggleTvFollow): Either<ToggleTvFollowHandlerError, Unit> = either {
        val show = tvShowRepository.findById(command.tvShowId) ?: raise(ToggleTvFollowHandlerError.TvShowNotFound)

        tvShowRepository.toggleFollow(command.userId, show.id!!)
    }
}
