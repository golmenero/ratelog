package org.raterr.search

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.TmdbId
import org.raterr.movie.MovieRepository
import org.raterr.movie.rating.RatingRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.user.User
import java.time.LocalDate

data class SearchQuery(
    val query: String,
    val userId: User.Id,
)

data class SearchResultItem(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val year: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val type: String,
    val isFollowed: Boolean = false,
    val canRate: Boolean = true,
    val canFollow: Boolean = true,
    val movieRating: MovieRatingInfo? = null,
    val tvSeasonRatings: List<TvSeasonRatingInfo> = emptyList()
)

data class MovieRatingInfo(
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val score: Double
)

data class TvSeasonRatingInfo(
    val seasonNumber: Int,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val score: Double
)

@org.springframework.stereotype.Service
class SearchHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(query: SearchQuery): Either<SearchHandlerError, List<SearchResultItem>> = either {
        if (query.query.isBlank()) return@either emptyList()
        val movies = searchMovies(query.query).bind().take(6)
        val shows = searchTvShows(query.query).bind().take(6)

        interleave(movies, shows)
    }

    private fun interleave(movies: List<SearchResultItem>, shows: List<SearchResultItem>): List<SearchResultItem> {
        val result = mutableListOf<SearchResultItem>()
        val maxSize = maxOf(movies.size, shows.size)
        for (i in 0 until maxSize) {
            if (i < movies.size) result.add(movies[i])
            if (i < shows.size) result.add(shows[i])
        }
        return result
    }

    private fun searchMovies(query: String): Either<SearchHandlerError, List<SearchResultItem>> =
        either {
            val movies = tmdbClient.searchMovies(query).bind()

            movies.map { tmdbMovie ->
                val movie = tmdbMovie.id.let(::TmdbId).let(movieRepository::findByTmdbId)
                val rating = movie?.id?.let(ratingRepository::findFirstByMovieId)
                val isReleased = tmdbMovie.releaseDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) <= LocalDate.now() } ?: false

                val movieRating = rating?.let {
                    MovieRatingInfo(
                        directing = it.directing.value,
                        cinematography = it.cinematography.value,
                        acting = it.acting.value,
                        soundtrack = it.soundtrack.value,
                        screenplay = it.screenplay.value,
                        score = it.score.value,
                    )
                }

                SearchResultItem(
                    tmdbId = tmdbMovie.id,
                    title = tmdbMovie.title,
                    overview = tmdbMovie.overview,
                    year = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                    posterPath = tmdbMovie.posterPath,
                    tmdbVoteAverage = tmdbMovie.voteAverage,
                    type = MediaType.movie.name,
                    isFollowed = movie?.followed ?: false,
                    canRate = isReleased,
                    canFollow = rating == null,
                    movieRating = movieRating
                )
            }
        }

    private fun searchTvShows(query: String): Either<SearchHandlerError,List<SearchResultItem>> =
        either {
            val tvshows = tmdbClient.searchTvShows(query).bind()

            tvshows.map { tmdbShow ->
                val show = tmdbShow.id.let(::TmdbId).let(tvShowRepository::findByTmdbId)
                val rating = show?.id?.let(tvRatingRepository::findFirstByTvShowId)
                val isReleased = tmdbShow.firstAirDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) <= LocalDate.now() } ?: false


                val tvSeasonRatings = rating?.seasonRatings?.map { seasonRating ->
                    TvSeasonRatingInfo(
                        seasonNumber = seasonRating.seasonNumber.value,
                        directing = seasonRating.directing.value,
                        cinematography = seasonRating.cinematography.value,
                        acting = seasonRating.acting.value,
                        soundtrack = seasonRating.soundtrack.value,
                        screenplay = seasonRating.screenplay.value,
                        score = seasonRating.score.value
                    )
                } ?: emptyList()

                SearchResultItem(
                    tmdbId = tmdbShow.id,
                    title = tmdbShow.name,
                    overview = tmdbShow.overview,
                    year = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                    posterPath = tmdbShow.posterPath,
                    tmdbVoteAverage = tmdbShow.voteAverage,
                    type = MediaType.tvshow.name,
                    isFollowed = show?.followed ?: false,
                    canRate = isReleased,
                    canFollow = rating == null,
                    tvSeasonRatings = tvSeasonRatings
                )
        }
    }
}
