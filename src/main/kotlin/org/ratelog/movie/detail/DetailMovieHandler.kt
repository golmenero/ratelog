package org.ratelog.movie.detail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.Status
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Url
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieRepository
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import org.springframework.stereotype.Component
import java.time.LocalDate

data class GetMovieDetail(
    val userId: User.Id,
    val tmdbId: TmdbId
)

data class GetMovieDetailResult(
    val movie: Movie,
    val isRated: Boolean,
    val directing: Double?,
    val cinematography: Double?,
    val acting: Double?,
    val soundtrack: Double?,
    val screenplay: Double?,
    val score: Double?,
    val isFollowed: Boolean,
)

@Component
class DetailMovieHandler(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(query: GetMovieDetail): Either<DetailMovieHandlerError, GetMovieDetailResult> = either {
        val tmdbMovie = query.tmdbId.value.let(tmdbClient::movieDetails).bind()

        val movie = query.tmdbId.let(movieRepository::findByTmdbId)
        if (movie == null) tmdbMovie.let(movieRepository::save)

        val updatedMovie = movieRepository.findByTmdbId(query.tmdbId)!!

        val rating = ratingRepository.findByMovieIdAndUserId(updatedMovie.id!!, query.userId)
        val isFollowed = movieRepository.isFollowed(query.userId, updatedMovie.id)

        GetMovieDetailResult(
            movie = updatedMovie,
            isRated = rating != null,
            directing = rating?.directing?.value,
            cinematography = rating?.cinematography?.value,
            acting = rating?.acting?.value,
            soundtrack = rating?.soundtrack?.value,
            screenplay = rating?.screenplay?.value,
            score = rating?.score?.value,
            isFollowed = isFollowed,
        )
    }
}
