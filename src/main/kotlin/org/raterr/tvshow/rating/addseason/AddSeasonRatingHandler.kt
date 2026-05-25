package org.raterr.tvshow.rating.addseason

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.Score
import org.raterr.TmdbId
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.get.GetTvShow
import org.raterr.tvshow.get.GetTvShowHandler
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.tvshow.rating.rank.RankTvRating
import org.raterr.tvshow.rating.rank.RankTvRatingHandler
import org.raterr.user.User
import org.springframework.stereotype.Component
import java.time.Instant

data class AddSeasonRating(
    val tmdbId: TmdbId,
    val seasonNumber: Int,
    val userId: User.Id,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
)

@Component
class AddSeasonRatingHandler(
    private val getTvShowHandler: GetTvShowHandler,
    private val tvShowRepository: TvShowRepository,
    private val rankTvRatingHandler: RankTvRatingHandler,
) {
    fun handle(command: AddSeasonRating): Either<AddSeasonRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { value ->
            ensure(value in 1.0..10.0) { AddSeasonRatingHandlerError.InvalidRatingValue }
        }

        val show = GetTvShow(TmdbId(command.tmdbId.value))
            .let(getTvShowHandler::handle)
            .mapLeft { AddSeasonRatingHandlerError.TvShowNotFound }
            .bind()

        val seasonNumber = SeasonRating.SeasonNumber(command.seasonNumber)
        val existingRating = seasonRatingRepository.findByTvShowIdAndSeasonNumberAndUserId(show.id!!, seasonNumber, command.userId).firstOrNull()
        ensure(existingRating == null) { AddSeasonRatingHandlerError.RatingAlreadyExists }

        SeasonRating(
            id = null,
            tvShowId = show.id,
            seasonNumber = seasonNumber,
            userId = command.userId,
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            createdAt = Instant.now(),
        ).let(seasonRatingRepository::save)

        command.userId.let(::RankTvRating).let(rankTvRatingHandler::handle)

        if (show.followed) {
            show.toggleFollow(System.currentTimeMillis()).let(tvShowRepository::save)
        }
    }
}
