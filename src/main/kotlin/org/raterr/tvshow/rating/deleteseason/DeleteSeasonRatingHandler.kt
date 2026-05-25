package org.raterr.tvshow.rating.deleteseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.SeasonNumber
import org.raterr.TmdbId
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.rating.TvRating
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.tvshow.rating.rank.RankTvRating
import org.raterr.tvshow.rating.rank.RankTvRatingHandler
import org.raterr.user.User
import org.springframework.stereotype.Component
import java.time.Instant

data class DeleteSeasonRating(
    val tmdbId: TmdbId,
    val seasonNumber: SeasonNumber,
    val userId: User.Id,
)

@Component
class DeleteSeasonRatingHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val rankTvRatingHandler: RankTvRatingHandler,
) {
    fun handle(command: DeleteSeasonRating): Either<DeleteSeasonRatingHandlerError, Unit> = either {
        val show = command.tmdbId.let(tvShowRepository::findByTmdbId)

        ensure(show != null && show.id != null) { DeleteSeasonRatingHandlerError.TvShowNotFound }

        val tvRating = tvRatingRepository.findFirstByTvShowId(show.id) ?:
            raise(DeleteSeasonRatingHandlerError.RatingNotFound)

        val updatedTvRating = tvRating.deleteSeasonRating(command.seasonNumber)

        updatedTvRating.let(tvRatingRepository::save)
        if (updatedTvRating.seasonRatings.isEmpty()) tvRatingRepository.deleteById(tvRating.id!!)

        command.userId.let(::RankTvRating).let(rankTvRatingHandler::handle)
    }
}
