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

@Component
class GetTvShowHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(query: GetTvShow): Either<GetTvShowHandlerError, TvShow> = either {
        val tmdbShow = query.tmdbId.value.let(tmdbClient::tvShowDetails).bind()
        val genres = tmdbShow.genres.map { Genre.valueOf(it.name) }

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

        show.let(tvShowRepository::save)
    }
}
