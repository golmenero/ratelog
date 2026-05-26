package org.raterr.tvshow.detail

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Genre
import org.raterr.Overview
import org.raterr.Title
import org.raterr.TmdbId
import org.raterr.Url
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.tvshow.rating.TvRatingRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetTvShowDetail(val tmdbId: TmdbId)

data class GetTvShowDetailResult(
    val show: TvShow,
    val seasons: List<SeasonInfo>,
    val overallScore: Double?,
)

data class SeasonInfo(
    val seasonNumber: Int,
    val episodeCount: Int?,
    val airDate: String?,
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
        val genres = tmdbShow.genres.mapNotNull { Genre.fromValue(it.name) }

        val show = query.tmdbId
            .let(tvShowRepository::findByTmdbId)
            ?.copy(
                name = tmdbShow.name.let(::Title),
                originalName = tmdbShow.originalName?.let(::Title),
                overview = tmdbShow.overview?.let(::Overview),
                firstAirDate = tmdbShow.firstAirDate?.let(LocalDate::parse),
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath?.let(::Url),
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = genres
            )
            ?: TvShow(
                id = null,
                tmdbId = tmdbShow.id.let(::TmdbId),
                name = tmdbShow.name.let(::Title),
                originalName = tmdbShow.originalName?.let(::Title),
                overview = tmdbShow.overview?.let(::Overview),
                firstAirDate = tmdbShow.firstAirDate?.let(LocalDate::parse),
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath?.let(::Url),
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = genres
            )

        val updatedShow = show.let(tvShowRepository::save)

        val rating = tvRatingRepository.findFirstByTvShowId(updatedShow.id!!)
        val ratingMap = rating?.seasonRatings?.associateBy { it.seasonNumber.value } ?: emptyMap()

        val seasons = tmdbShow.seasons
            .filter { it.seasonNumber > 0 }
            .map { tmdbSeason ->
                val seasonRating = ratingMap[tmdbSeason.seasonNumber]
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
                    seasonNumber = tmdbSeason.seasonNumber,
                    episodeCount = tmdbSeason.episodeCount,
                    airDate = tmdbSeason.airDate,
                    rating = ratingInfo,
                )
            }

        GetTvShowDetailResult(
            show = updatedShow,
            seasons = seasons,
            overallScore = rating?.score?.value,
        )
    }
}
