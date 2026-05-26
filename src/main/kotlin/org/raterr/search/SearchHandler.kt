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
)

@org.springframework.stereotype.Service
class SearchHandler(
    private val tmdbClient: TmdbClient,
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
                SearchResultItem(
                    tmdbId = tmdbMovie.id,
                    title = tmdbMovie.title,
                    overview = tmdbMovie.overview,
                    year = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                    posterPath = tmdbMovie.posterPath,
                    tmdbVoteAverage = tmdbMovie.voteAverage,
                    type = MediaType.movie.name,
                )
            }
        }

    private fun searchTvShows(query: String): Either<SearchHandlerError,List<SearchResultItem>> =
        either {
            val tvshows = tmdbClient.searchTvShows(query).bind()

            tvshows.map { tmdbShow ->
                SearchResultItem(
                    tmdbId = tmdbShow.id,
                    title = tmdbShow.name,
                    overview = tmdbShow.overview,
                    year = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                    posterPath = tmdbShow.posterPath,
                    tmdbVoteAverage = tmdbShow.voteAverage,
                    type = MediaType.tvshow.name,
                )
        }
    }
}
