package org.raterr.movie

fun aMovie(
    id: Long? = null,
    tmdbId: Int = 1,
    title: String = "Test Movie",
    originalTitle: String? = "Test Movie",
    overview: String? = "Test overview",
    releaseDate: String? = "2024-01-01",
    releaseYear: Int? = 2024,
    posterPath: String? = "/test-poster.jpg",
    tmdbVoteAverage: Double? = 7.5,
    genres: String? = "Action,Adventure"
): Movie =
    Movie(
        id = id,
        tmdbId = tmdbId,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        releaseDate = releaseDate,
        releaseYear = releaseYear,
        posterPath = posterPath,
        tmdbVoteAverage = tmdbVoteAverage,
        genres = genres
    )