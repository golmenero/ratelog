package org.raterr.tvshow.rating.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.TmdbId
import org.raterr.tvshow.rating.rank.RankTvRatingHandler
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.tvshow.rating.rank.RankTvRating
import org.raterr.tvshow.TvShowRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

data class DeleteTvRating(
    val tmdbId: TmdbId,
    val userId: User.Id,
)

@Component
class DeleteTvRatingHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val rankTvRatingHandler: RankTvRatingHandler,
) {
    fun handle(command: DeleteTvRating): Either<DeleteTvRatingHandlerError, Unit> = either {
        val show = command.tmdbId.let(tvShowRepository::findByTmdbId)

        ensure(show != null && show.id != null) { DeleteTvRatingHandlerError.TvShowNotFound }

        val deletedCount = tvRatingRepository.deleteByTvShowIdAndUserId(show.id, command.userId)
        ensure(deletedCount > 0) { DeleteTvRatingHandlerError.RatingNotFound }

        command.userId.let(::RankTvRating).let(rankTvRatingHandler::handle)
    }
}
