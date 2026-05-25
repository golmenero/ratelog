package org.raterr.tvshow.rating.addseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.Score
import org.raterr.SeasonNumber
import org.raterr.TmdbId
import org.raterr.tvshow.get.GetTvShow
import org.raterr.tvshow.get.GetTvShowHandler
import org.raterr.tvshow.rating.TvRating
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Component
import java.time.Instant

data class AddSeasonRating(
    val tmdbId: TmdbId,
    val seasonNumber: SeasonNumber,
    val userId: User.Id,
    val directing: Score,
    val cinematography: Score,
    val acting: Score,
    val soundtrack: Score,
    val screenplay: Score,
)

@Component
class AddSeasonRatingHandler(
    private val getTvShowHandler: GetTvShowHandler,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(command: AddSeasonRating): Either<AddSeasonRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { score ->
            ensure(score.value in 1.0..10.0) { AddSeasonRatingHandlerError.InvalidRatingValue }
        }

        val result = GetTvShow(TmdbId(command.tmdbId.value))
            .let(getTvShowHandler::handle)
            .mapLeft { AddSeasonRatingHandlerError.TvShowNotFound }
            .bind()

        val tvRating = tvRatingRepository.findFirstByTvShowId(result.show.id!!) ?: TvRating.create(result.show.id, command.userId, Instant.now())
        tvRating.addSeasonRating(
            seasonNumber = command.seasonNumber,
            directing = command.directing,
            cinematography = command.cinematography,
            acting = command.acting,
            soundtrack = command.soundtrack,
            screenplay = command.screenplay,
            createdAt = Instant.now(),
        ).let(tvRatingRepository::save)
    }
}
