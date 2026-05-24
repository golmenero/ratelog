package org.raterr.tvshow

import org.raterr.Genre
import org.raterr.Overview
import org.raterr.TmdbId
import org.raterr.Title
import org.raterr.Url
import java.time.LocalDate

fun aTvShow(
    id: TvShow.Id? = TvShow.Id(1),
    tmdbId: Int = 1,
    name: String = "Test TvShow",
    originalName: String? = "Test TvShow",
    overview: String? = "Test overview",
    firstAirDate: String? = "2024-01-01",
    firstAirYear: Int? = 2024,
    posterPath: String? = "/test-poster.jpg",
    tmdbVoteAverage: Double? = 7.5,
    genres: List<Genre> = listOf(Genre.Action, Genre.Adventure)
): TvShow =
    TvShow(
        id = id,
        tmdbId = TmdbId(tmdbId),
        name = Title(name),
        originalName = originalName?.let { Title(it) },
        overview = overview?.let { Overview(it) },
        firstAirDate = firstAirDate?.let { LocalDate.parse(it) },
        firstAirYear = firstAirYear,
        posterPath = posterPath?.let { Url(it) },
        tmdbVoteAverage = tmdbVoteAverage,
        genres = genres
    )
