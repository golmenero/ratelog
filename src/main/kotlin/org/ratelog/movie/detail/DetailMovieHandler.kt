package org.ratelog.movie.detail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieRepository
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class GetMovieDetail(
    val userId: User.Id,
    val tmdbId: TmdbId,
    val lang: Lang,
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
    val review: String?,
    val isFollowed: Boolean,
)

@Component
class DetailMovieHandler(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    @Transactional
    fun handle(query: GetMovieDetail): Either<DetailMovieHandlerError, GetMovieDetailResult> = either {
        val tmdbMovie = tmdbClient.movieDetails(query.tmdbId, query.lang).bind()

        val movie = query.tmdbId.let(movieRepository::findByTmdbId)
        val savedMovie = movie ?: tmdbMovie.let(movieRepository::save)

        val rating = ratingRepository.findByMovieIdAndUserId(savedMovie.id!!, query.userId)
        val isFollowed = movieRepository.isFollowed(query.userId, savedMovie.id)

        GetMovieDetailResult(
            movie = savedMovie,
            isRated = rating != null,
            directing = rating?.directing?.value,
            cinematography = rating?.cinematography?.value,
            acting = rating?.acting?.value,
            soundtrack = rating?.soundtrack?.value,
            screenplay = rating?.screenplay?.value,
            score = rating?.score?.value,
            review = rating?.review?.value,
            isFollowed = isFollowed,
        )
    }
}
