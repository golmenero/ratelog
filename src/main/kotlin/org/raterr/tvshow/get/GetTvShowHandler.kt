package org.raterr.tvshow.get

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Genre
import org.raterr.Overview
import org.raterr.Title
import org.raterr.tmdb.TmdbClient
import org.raterr.TmdbId
import org.raterr.Url
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetTvShow(val tmdbId: TmdbId)

data class GetTvShowResult(
    val show: TvShow,
    val seasons: List<TmdbSeasonInfo>
)

data class TmdbSeasonInfo(
    val seasonNumber: Int,
    val episodeCount: Int?,
    val airDate: String?
)

@Component
class GetTvShowHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(query: GetTvShow): Either<GetTvShowHandlerError, GetTvShowResult> = either {
        val tmdbShow = query.tmdbId.value.let(tmdbClient::tvShowDetails).bind()
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

        val seasons = tmdbShow.seasons
            .filter { it.seasonNumber > 0 }
            .map { s ->
                TmdbSeasonInfo(
                    seasonNumber = s.seasonNumber,
                    episodeCount = s.episodeCount,
                    airDate = s.airDate
                )
            }

        GetTvShowResult(
            show = show.let(tvShowRepository::save),
            seasons = seasons
        )
    }
}
