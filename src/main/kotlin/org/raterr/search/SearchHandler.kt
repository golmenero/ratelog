package org.raterr.search

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.TmdbId
import org.raterr.follow.FollowRepository
import org.raterr.movie.MovieRepository
import org.raterr.rating.RatingRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvrating.TvRatingRepository
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
    val canFollow: Boolean = true
)

@org.springframework.stereotype.Service
class SearchHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val followRepository: FollowRepository,
) {
    fun handle(query: SearchQuery): Either<SearchHandlerError, List<SearchResultItem>> = either {
        if (query.query.isBlank()) return@either emptyList()
        val movies = searchMovies(query.query, query.userId).bind().take(6)
        val shows = searchTvShows(query.query, query.userId).bind().take(6)

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

    private fun searchMovies(query: String, userId: User.Id): Either<SearchHandlerError, List<SearchResultItem>> =
        either {
            val movies = tmdbClient.searchMovies(query).bind()

            movies.map { tmdbMovie ->
                val movie = tmdbMovie.id.let(::TmdbId).let(movieRepository::findByTmdbId)
                val rating = movie?.id?.value?.let(ratingRepository::findFirstByMovieId)
                val isFollowed = userId.let {
                    followRepository.existsByUserIdAndContentTypeAndContentTmdbId(it.value, MediaType.movie.name, tmdbMovie.id)
                }
                val isReleased = tmdbMovie.releaseDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) <= LocalDate.now() } ?: false

                SearchResultItem(
                    tmdbId = tmdbMovie.id,
                    title = tmdbMovie.title,
                    overview = tmdbMovie.overview,
                    year = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                    posterPath = tmdbMovie.posterPath,
                    tmdbVoteAverage = tmdbMovie.voteAverage,
                    type = MediaType.movie.name,
                    isFollowed = isFollowed,
                    canRate = isReleased,
                    canFollow = rating == null
                )
            }
        }

    private fun searchTvShows(query: String, userId: User.Id): Either<SearchHandlerError,List<SearchResultItem>> =
        either {
            val tvshows = tmdbClient.searchTvShows(query).bind()

            tvshows.map { tmdbShow ->
                val show = tmdbShow.id.let(::TmdbId).let(tvShowRepository::findByTmdbId)
                val rating = show?.id?.value?.let(tvRatingRepository::findFirstByTvShowId)
                val isFollowed = userId.let {
                    followRepository.existsByUserIdAndContentTypeAndContentTmdbId(it.value, MediaType.tvshow.name, tmdbShow.id)
                }
                val isReleased = tmdbShow.firstAirDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) <= LocalDate.now() } ?: false


                SearchResultItem(
                    tmdbId = tmdbShow.id,
                    title = tmdbShow.name,
                    overview = tmdbShow.overview,
                    year = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                    posterPath = tmdbShow.posterPath,
                    tmdbVoteAverage = tmdbShow.voteAverage,
                    type = MediaType.tvshow.name,
                    isFollowed = isFollowed,
                    canRate = isReleased,
                    canFollow = rating == null
                )
        }
    }
}
