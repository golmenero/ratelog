package org.ratelog.movie.detail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieDescriptionRepository
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
    val id: Long,
    val tmdbId: Int,
    val title: String,
    val originalTitle: String,
    val overview: String?,
    val releaseDate: String?,
    val releaseYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val genres: List<String>,
    val status: String?,
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
    private val movieDescriptionRepository: MovieDescriptionRepository,
    private val ratingRepository: RatingRepository,
) {
    @Transactional
    fun handle(query: GetMovieDetail): Either<DetailMovieHandlerError, GetMovieDetailResult> = either {
        val tmdbMovie = tmdbClient.movieDetails(query.tmdbId).bind()

        val movie = query.tmdbId.let(movieRepository::findByTmdbId)
        val savedMovie = movie ?: tmdbMovie.let(movieRepository::save)

        if (!movieDescriptionRepository.existsAnyByTmdbId(savedMovie.tmdbId)) {
            tmdbClient.movieTranslations(savedMovie.tmdbId, savedMovie.originalTitle).fold(
                { },
                { movieDescriptionRepository.saveAll(it) }
            )
        }

        val description = movieDescriptionRepository.findByTmdbIdAndLang(savedMovie.tmdbId, query.lang)
        val title = description?.title?.value ?: savedMovie.originalTitle.value
        val overview = description?.overview?.value

        val rating = ratingRepository.findByMovieIdAndUserId(savedMovie.id!!, query.userId)
        val isFollowed = movieRepository.isFollowed(query.userId, savedMovie.id)

        GetMovieDetailResult(
            id = savedMovie.id.value,
            tmdbId = savedMovie.tmdbId.value,
            title = title,
            originalTitle = savedMovie.originalTitle.value,
            overview = overview,
            releaseDate = savedMovie.releaseDate?.toString(),
            releaseYear = savedMovie.releaseYear,
            genres = savedMovie.genres.map { it.value },
            status = savedMovie.status?.value,
            posterPath = savedMovie.posterPath?.value,
            tmdbVoteAverage = savedMovie.tmdbVoteAverage,
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
