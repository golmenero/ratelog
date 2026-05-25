package org.raterr.tvrating.add

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.raterr.Score
import org.raterr.TmdbId
import org.raterr.follow.FollowRepository
import org.raterr.tvrating.TvRating
import org.raterr.tvshow.get.GetTvShow
import org.raterr.tvshow.get.GetTvShowHandler
import org.raterr.tvrating.rank.RankTvRatingHandler
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvrating.rank.RankTvRating
import org.raterr.user.User
import org.springframework.stereotype.Component
import java.time.Instant

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
    private val rankTvRatingHandler: RankTvRatingHandler,
) {
    fun handle(command: AddTvRating): Either<AddTvRatingHandlerError, Unit> = either {
        listOf(
            command.directing,
            command.cinematography,
            command.acting,
            command.soundtrack,
            command.screenplay
        ).forEach { value ->
            ensure(value in 1.0..10.0) { AddTvRatingHandlerError.InvalidRatingValue }
        }

        val show = GetTvShow(TmdbId(command.tmdbId.value))
            .let(getTvShowHandler::handle)
            .mapLeft { AddTvRatingHandlerError.TvShowNotFound }
            .bind()

        val existingRating = tvRatingRepository.findByTvShowIdAndUserId(show.id!!, command.userId).firstOrNull()
        ensure(existingRating == null) { AddTvRatingHandlerError.RatingAlreadyExists }

        TvRating(
            id = null,
            tvShowId = show.id,
            userId = command.userId,
            directing = Score(command.directing),
            cinematography = Score(command.cinematography),
            acting = Score(command.acting),
            soundtrack = Score(command.soundtrack),
            screenplay = Score(command.screenplay),
            createdAt = Instant.now(),
            rank = TvRating.Rank(0)
        ).let(tvRatingRepository::save)

        command.userId.let(::RankTvRating).let(rankTvRatingHandler::handle)

        followRepository.findByUserIdAndContentTypeAndContentTmdbId(
            command.userId.value,
            "tvshow",
            command.tmdbId.value
        ).ifPresent(followRepository::delete)
    }
}
