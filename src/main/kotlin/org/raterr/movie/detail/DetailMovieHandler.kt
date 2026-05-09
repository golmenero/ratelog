package org.raterr.movie.detail

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.tmdb.TmdbClient
import org.raterr.TmdbId
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.springframework.stereotype.Controller

data class DetailMovie(val tmdbId: TmdbId)

@Controller
class DetailMovieHandler(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
) {
    fun handle(query: DetailMovie): Either<DetailMovieHandlerError, Movie> = either {
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
