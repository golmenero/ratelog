package org.ratelog.tvshow.rating.deleteseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.ratelog.SeasonNumber
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component

data class DeleteSeasonRating(
    val tvShowId: TvShow.Id,
    val seasonNumber: SeasonNumber,
    val userId: User.Id,
)

@Component
class DeleteSeasonRatingHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(command: DeleteSeasonRating): Either<DeleteSeasonRatingHandlerError, Unit> = either {
        val show = command.tvShowId.let(tvShowRepository::findById)

        ensure(show != null && show.id != null) { DeleteSeasonRatingHandlerError.TvShowNotFound }

        val tvRating = tvRatingRepository.findByTvShowIdAndUserId(show.id, command.userId) ?:
            raise(DeleteSeasonRatingHandlerError.RatingNotFound)

        val updatedTvRating = tvRating.deleteSeasonRating(command.seasonNumber)

        updatedTvRating.let(tvRatingRepository::save)
        if (updatedTvRating.seasonRatings.isEmpty()) tvRatingRepository.deleteById(tvRating.id!!)
    }
}
