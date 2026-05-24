package org.raterr.movie

import org.raterr.Genre
import org.raterr.Overview
import org.raterr.TmdbId
import org.raterr.Title
import org.raterr.Url

fun aMovie(
    id: Movie.Id? = Movie.Id(1),
    tmdbId: Int = 1,
    title: String = "Test Movie",
    originalTitle: String? = "Test Movie",
    overview: String? = "Test overview",
    releaseDate: String? = "2024-01-01",
    releaseYear: Int? = 2024,
    posterPath: String? = "/test-poster.jpg",
    tmdbVoteAverage: Double? = 7.5,
    genres: List<Genre> = listOf(Genre.Action, Genre.Adventure)
): Movie =
    Movie(
        id = id,
        tmdbId = TmdbId(tmdbId),
        title = Title(title),
        originalTitle = originalTitle?.let { Title(it) },
        overview = overview?.let { Overview(it) },
        releaseDate = releaseDate?.let { java.time.LocalDate.parse(it) },
        releaseYear = releaseYear,
        posterPath = posterPath?.let { Url(it) },
        tmdbVoteAverage = tmdbVoteAverage,
        genres = genres
    )