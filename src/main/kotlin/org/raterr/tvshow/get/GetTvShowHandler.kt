package org.raterr.tvshow.get

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.tmdb.TmdbClient
import org.raterr.TmdbId
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.springframework.stereotype.Component

data class GetTvShow(val tmdbId: TmdbId)

@Component
class GetTvShowHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(query: GetTvShow): Either<GetTvShowHandlerError, TvShow> = either {
        val tmdbShow = query.tmdbId.value.let(tmdbClient::tvShowDetails).bind()
        val genres = tmdbShow.genres.joinToString(",") { it.name }

        val show = query.tmdbId
            .value
            .let(tvShowRepository::findByTmdbId)
            .orElse(null)
            ?.copy(
                name = tmdbShow.name,
                originalName = tmdbShow.originalName,
                overview = tmdbShow.overview,
                firstAirDate = tmdbShow.firstAirDate,
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = genres
            )
            ?: TvShow(
                tmdbId = tmdbShow.id,
                name = tmdbShow.name,
                originalName = tmdbShow.originalName,
                overview = tmdbShow.overview,
                firstAirDate = tmdbShow.firstAirDate,
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = genres
            )

        show.let(tvShowRepository::save)
    }
}
