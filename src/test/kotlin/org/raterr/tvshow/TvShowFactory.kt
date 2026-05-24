package org.raterr.tvshow

fun aTvShow(
    id: Long? = null,
    tmdbId: Int = 1,
    name: String = "Test TvShow",
    originalName: String? = "Test TvShow",
    overview: String? = "Test overview",
    firstAirDate: String? = "2024-01-01",
    firstAirYear: Int? = 2024,
    posterPath: String? = "/test-poster.jpg",
    tmdbVoteAverage: Double? = 7.5,
    genres: String? = "Action,Adventure"
): TvShow =
    TvShow(
        id = id,
        tmdbId = tmdbId,
        name = name,
        originalName = originalName,
        overview = overview,
        firstAirDate = firstAirDate,
        firstAirYear = firstAirYear,
        posterPath = posterPath,
        tmdbVoteAverage = tmdbVoteAverage,
        genres = genres
    )
