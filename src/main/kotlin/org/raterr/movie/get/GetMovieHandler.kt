package org.raterr.movie.get

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Genre
import org.raterr.Overview
import org.raterr.Title
import org.raterr.tmdb.TmdbClient
import org.raterr.TmdbId
import org.raterr.Url
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetMovie(val tmdbId: TmdbId)

@Component
class GetMovieHandler(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
) {
    fun handle(query: GetMovie): Either<GetMovieHandlerError, Movie> = either {
        val tmdbMovie = query.tmdbId.value.let(tmdbClient::movieDetails).bind()
        val genres = tmdbMovie.genres.mapNotNull { Genre.fromValue(it.name) }

        val movie = query.tmdbId
            .let(movieRepository::findByTmdbId)
            ?.copy(
                title = tmdbMovie.title.let(::Title),
                originalTitle = tmdbMovie.originalTitle?.let(::Title),
                overview = tmdbMovie.overview?.let(::Overview),
                releaseDate = tmdbMovie.releaseDate?.let(LocalDate::parse),
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath?.let(::Url),
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )
            ?: Movie(
                id = null,
                tmdbId = tmdbMovie.id.let(::TmdbId),
                title = tmdbMovie.title.let(::Title),
                originalTitle = tmdbMovie.originalTitle?.let(::Title),
                overview = tmdbMovie.overview?.let(::Overview),
                releaseDate = tmdbMovie.releaseDate?.let(LocalDate::parse),
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath?.let(::Url),
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )

        movie.let(movieRepository::save)
    }
}
