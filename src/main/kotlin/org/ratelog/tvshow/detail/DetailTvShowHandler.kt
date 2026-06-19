package org.ratelog.tvshow.detail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class GetTvShowDetail(
    val userId: User.Id,
    val tmdbId: TmdbId,
    val lang: Lang,
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
    val review: String?,
)

@Component
class DetailTvShowHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    @Transactional
    fun handle(query: GetTvShowDetail): Either<DetailTvShowHandlerError, GetTvShowDetailResult> = either {
        val tmdbShow = tmdbClient.tvShowDetails(query.tmdbId, query.lang)
            .mapLeft { DetailTvShowHandlerError.TvShowNotFound }
            .bind()

        val show = query.tmdbId.let(tvShowRepository::findByTmdbId)
        val savedShow = show ?: tmdbShow.let(tvShowRepository::save)

        val rating = tvRatingRepository.findByTvShowIdAndUserId(savedShow.id!!, query.userId)
        val isFollowed = tvShowRepository.isFollowed(query.userId, savedShow.id)
        val ratingMap = rating?.seasonRatings?.associateBy { it.seasonNumber.value } ?: emptyMap()

        val lastSeason = savedShow.lastSeasonNumber ?: 0
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
                        review = sr.review?.value,
                    )
                }
                SeasonInfo(
                    seasonNumber = seasonNumber,
                    rating = ratingInfo,
                )
            }

        GetTvShowDetailResult(
            show = savedShow,
            seasons = seasons,
            overallScore = rating?.score?.value,
            isRated = rating != null,
            isFollowed = isFollowed,
        )
    }
}
