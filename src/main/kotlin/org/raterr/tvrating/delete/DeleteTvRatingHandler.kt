package org.raterr.tvrating.delete

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvshow.TvShowRepository
import org.springframework.stereotype.Component

data class DeleteTvRating(
    val tmdbId: TmdbId,
    val userId: UserId,
)

@Component
class DeleteTvRatingHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(command: DeleteTvRating): Either<DeleteTvRatingHandlerError, Unit> = either {
        val show = tvShowRepository.findByTmdbId(command.tmdbId.value)
            .orElse(null)

        ensure(show != null) { DeleteTvRatingHandlerError.TvShowNotFound }

        val deletedCount = tvRatingRepository.deleteByTvShowIdAndUserId(show.id!!, command.userId.value)
        ensure(deletedCount > 0) { DeleteTvRatingHandlerError.RatingNotFound }
    }
}
