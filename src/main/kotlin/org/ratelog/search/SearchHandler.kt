package org.ratelog.search

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.tmdb.TmdbClient
import org.ratelog.user.User
import org.springframework.transaction.annotation.Transactional

data class SearchQuery(
    val query: String,
    val userId: User.Id,
    val lang: Lang,
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
    @Transactional
    fun handle(query: SearchQuery): Either<SearchHandlerError, List<SearchResultItem>> = either {
        if (query.query.isBlank()) return@either emptyList()
        val movies = searchMovies(query.query, query.lang).bind().take(6)
        val shows = searchTvShows(query.query, query.lang).bind().take(6)

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

    private fun searchMovies(query: String, lang: Lang): Either<SearchHandlerError, List<SearchResultItem>> =
        either {
            val movies = tmdbClient.searchMovies(query, lang).bind()

            movies.map {
                SearchResultItem(
                    tmdbId = it.id,
                    title = it.title,
                    overview = it.overview,
                    year = it.releaseDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                    posterPath = it.posterPath,
                    type = MediaType.movie.name,
                )
            }
        }

    private fun searchTvShows(query: String, lang: Lang): Either<SearchHandlerError,List<SearchResultItem>> =
        either {
            val tvshows = tmdbClient.searchTvShows(query, lang).bind()

            tvshows.map {
                SearchResultItem(
                    tmdbId = it.id,
                    title = it.name,
                    overview = it.overview,
                    year = it.firstAirDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                    posterPath = it.posterPath,
                    type = MediaType.tvshow.name,
                )
        }
    }
}
