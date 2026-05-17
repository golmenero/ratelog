package org.raterr.tmdb

import arrow.core.Either
import arrow.core.left
import arrow.core.right

class FakeTmdbClient(
    private val movies: Map<Int, TmdbMovie> = emptyMap(),
    private val tvShows: Map<Int, TmdbTvShow> = emptyMap()
) : TmdbClient("fake-key", "http://fake") {

    override fun movieDetails(tmdbId: Int): Either<TmdbError, TmdbMovie> =
        movies[tmdbId]?.right() ?: TmdbError.MovieNotFound.left()

    override fun tvShowDetails(tmdbId: Int): Either<TmdbError, TmdbTvShow> =
        tvShows[tmdbId]?.right() ?: TmdbError.TvShowNotFound.left()

    override fun searchMovies(query: String): Either<TmdbError, List<TmdbMovie>> =
        movies.values.filter { it.title.contains(query, ignoreCase = true) }.right()

    override fun searchTvShows(query: String): Either<TmdbError, List<TmdbTvShow>> =
        tvShows.values.filter { it.name.contains(query, ignoreCase = true) }.right()
}
