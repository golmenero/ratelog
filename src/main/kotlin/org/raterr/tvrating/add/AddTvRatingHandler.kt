package org.raterr.tvrating.add

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.follow.FollowRepository
import org.raterr.tvshow.get.GetTvShow
import org.raterr.tvshow.get.GetTvShowHandler
import org.raterr.tvrating.TvRating
import org.raterr.tvrating.TvRatingRankService
import org.raterr.tvrating.TvRatingRepository
import org.raterr.TmdbId
import org.raterr.user.User
import org.springframework.stereotype.Component

data class AddTvRating(
    val tmdbId: TmdbId,
    val userId: User.Id,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
)

@Component
class AddTvRatingHandler(
    private val getTvShowHandler: GetTvShowHandler,
    private val tvRatingRepository: TvRatingRepository,
    private val followRepository: FollowRepository,
    private val tvRatingRankService: TvRatingRankService,
) {
    fun handle(command: AddTvRating): Either<AddTvRatingHandlerError, Unit> = either {
        listOf(
            "directing" to command.directing,
            "cinematography" to command.cinematography,
            "acting" to command.acting,
            "soundtrack" to command.soundtrack,
            "screenplay" to command.screenplay
        ).forEach { (field, value) ->
            ensure(value in 1.0..10.0) { AddTvRatingHandlerError.InvalidRatingValue }
        }

        val show = GetTvShow(TmdbId(command.tmdbId.value))
            .let(getTvShowHandler::handle)
            .mapLeft { AddTvRatingHandlerError.TvShowNotFound }
            .bind()

        val existingRating = tvRatingRepository.findByTvShowIdAndUserId(show.id!!.value, command.userId.value).firstOrNull()
        ensure(existingRating == null) { AddTvRatingHandlerError.RatingAlreadyExists }

        TvRating(
            id = null,
            tvShowId = show.id.value,
            userId = command.userId.value,
            directing = command.directing,
            cinematography = command.cinematography,
            acting = command.acting,
            soundtrack = command.soundtrack,
            screenplay = command.screenplay,
            createdAtEpochMs = System.currentTimeMillis()
        ).let(tvRatingRepository::save)

        tvRatingRankService.recalculateRanks(command.userId.value)

        followRepository.findByUserIdAndContentTypeAndContentTmdbId(
            command.userId.value,
            "tvshow",
            command.tmdbId.value
        ).ifPresent(followRepository::delete)
    }
}
