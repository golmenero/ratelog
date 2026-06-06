package org.ratelog.tvshow.detail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.Status
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Url
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetTvShowDetail(
    val userId: User.Id,
    val tmdbId: TmdbId,
    )

data class GetTvShowDetailResult(
    val show: TvShow,
    val seasons: List<SeasonInfo>,
    val overallScore: Double?,
    val isRated: Boolean,
    val isFollowed: Boolean,
)

data class SeasonInfo(
    val seasonNumber: Int,
    val rating: SeasonRatingInfo?,
)

data class SeasonRatingInfo(
    val seasonNumber: Int,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val score: Double,
)

@Component
class DetailTvShowHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(query: GetTvShowDetail): Either<DetailTvShowHandlerError, GetTvShowDetailResult> = either {
        val tmdbShow = query.tmdbId.value.let(tmdbClient::tvShowDetails)
            .mapLeft { DetailTvShowHandlerError.TvShowNotFound }
            .bind()

        val show = query.tmdbId.let(tvShowRepository::findByTmdbId)
        if (show == null) tmdbShow.let(tvShowRepository::save)

        val updatedShow = query.tmdbId.let(tvShowRepository::findByTmdbId)!!

        val rating = tvRatingRepository.findByTvShowIdAndUserId(updatedShow.id!!, query.userId)
        val isFollowed = tvShowRepository.isFollowed(query.userId, updatedShow.id)
        val ratingMap = rating?.seasonRatings?.associateBy { it.seasonNumber.value } ?: emptyMap()

        val lastSeason = updatedShow.lastSeasonNumber ?: 0
        val seasons = (1..lastSeason)
            .map { seasonNumber ->
                val seasonRating = ratingMap[seasonNumber]
                val ratingInfo = seasonRating?.let { sr ->
                    SeasonRatingInfo(
                        seasonNumber = sr.seasonNumber.value,
                        directing = sr.directing.value,
                        cinematography = sr.cinematography.value,
                        acting = sr.acting.value,
                        soundtrack = sr.soundtrack.value,
                        screenplay = sr.screenplay.value,
                        score = sr.score.value,
                    )
                }
                SeasonInfo(
                    seasonNumber = seasonNumber,
                    rating = ratingInfo,
                )
            }

        GetTvShowDetailResult(
            show = updatedShow,
            seasons = seasons,
            overallScore = rating?.score?.value,
            isRated = rating != null,
            isFollowed = isFollowed,
        )
    }
}
