package org.raterr.tvshow.rating.deleteseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.TmdbId
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.rating.rank.RankTvRating
import org.raterr.tvshow.rating.rank.RankTvRatingHandler
import org.raterr.user.User
import org.springframework.stereotype.Component

data class DeleteSeasonRating(
    val tmdbId: TmdbId,
    val seasonNumber: Int,
    val userId: User.Id,
)

@Component
class DeleteSeasonRatingHandler(
    private val tvShowRepository: TvShowRepository,
    private val seasonRatingRepository: SeasonRatingRepository,
    private val rankTvRatingHandler: RankTvRatingHandler,
) {
    fun handle(command: DeleteSeasonRating): Either<DeleteSeasonRatingHandlerError, Unit> = either {
        val show = command.tmdbId.let(tvShowRepository::findByTmdbId)

        ensure(show != null && show.id != null) { DeleteSeasonRatingHandlerError.TvShowNotFound }

        val seasonNumber = org.raterr.tvshow.rating.season.SeasonRating.SeasonNumber(command.seasonNumber)
        val deletedCount = seasonRatingRepository.deleteByTvShowIdAndSeasonNumberAndUserId(show.id, seasonNumber, command.userId)
        ensure(deletedCount > 0) { DeleteSeasonRatingHandlerError.RatingNotFound }

        command.userId.let(::RankTvRating).let(rankTvRatingHandler::handle)
    }
}
