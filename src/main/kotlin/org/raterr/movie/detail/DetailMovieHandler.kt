package org.raterr.movie.detail

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.Genre
import org.raterr.Overview
import org.raterr.Title
import org.raterr.TmdbId
import org.raterr.Url
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.raterr.movie.rating.RatingRepository
import org.raterr.tmdb.TmdbClient
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetMovieDetail(val tmdbId: TmdbId)

data class GetMovieDetailResult(
    val movie: Movie,
    val directing: Double?,
    val cinematography: Double?,
    val acting: Double?,
    val soundtrack: Double?,
    val screenplay: Double?,
    val score: Double?,
)

@Component
class GetMovieDetailHandler(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(query: GetMovieDetail): Either<DetailMovieHandlerError, GetMovieDetailResult> = either {
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

        val updatedMovie = movie.let(movieRepository::save)

        val rating = ratingRepository.findFirstByMovieId(updatedMovie.id!!)

        GetMovieDetailResult(
            movie = updatedMovie,
            directing = rating?.directing?.value,
            cinematography = rating?.cinematography?.value,
            acting = rating?.acting?.value,
            soundtrack = rating?.soundtrack?.value,
            screenplay = rating?.screenplay?.value,
            score = rating?.score?.value,
        )
    }
}
