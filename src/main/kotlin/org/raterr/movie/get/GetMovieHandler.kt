package org.raterr.movie.get

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.tmdb.TmdbClient
import org.raterr.TmdbId
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.springframework.stereotype.Component

data class GetMovie(val tmdbId: TmdbId)

@Component
class GetMovieHandler(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
) {
    fun handle(query: GetMovie): Either<GetMovieHandlerError, Movie> = either {
        val tmdbMovie = query.tmdbId.value.let(tmdbClient::movieDetails).bind()
        val genres = tmdbMovie.genres.joinToString(",") { it.name }

        val movie = query.tmdbId
            .value
            .let(movieRepository::findByTmdbId)
            .orElse(null)
            ?.copy(
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )
            ?: Movie(
                id = null,
                tmdbId = tmdbMovie.id,
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )

        movie.let(movieRepository::save)
    }
}
