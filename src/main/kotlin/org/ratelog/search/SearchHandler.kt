package org.ratelog.search

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.MediaType
import org.ratelog.TmdbId
import org.ratelog.movie.MovieRepository
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
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
                    tmdbId = tmdbMovie.tmdbId.value,
                    title = tmdbMovie.title.value,
                    overview = tmdbMovie.overview?.value,
                    year = tmdbMovie.releaseDate?.year,
                    posterPath = tmdbMovie.posterPath?.value,
                    type = MediaType.movie.name,
                )
            }
        }

    private fun searchTvShows(query: String): Either<SearchHandlerError,List<SearchResultItem>> =
        either {
            val tvshows = tmdbClient.searchTvShows(query).bind()

            tvshows.map { tmdbShow ->
                SearchResultItem(
                    tmdbId = tmdbShow.tmdbId.value,
                    title = tmdbShow.name.value,
                    overview = tmdbShow.overview?.value,
                    year = tmdbShow.firstAirDate?.year,
                    posterPath = tmdbShow.posterPath?.value,
                    type = MediaType.tvshow.name,
                )
        }
    }
}
