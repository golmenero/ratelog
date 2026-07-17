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
    val mediaType: MediaType?,
    val page: Int = 1,
)

data class SearchResult(
    val items: List<SearchResultItem>,
    val hasMore: Boolean,
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
    fun handle(query: SearchQuery): Either<SearchHandlerError, SearchResult> = either {
        if (query.query.isBlank()) return@either SearchResult(emptyList(), false)

        when (query.mediaType) {
            MediaType.movie -> {
                val movies = searchMovies(query.query, query.lang, query.page).bind()
                val items = movies.first.take(PAGE_SIZE)
                SearchResult(items, movies.first.size > PAGE_SIZE || movies.second > query.page)
            }
            MediaType.tvshow -> {
                val shows = searchTvShows(query.query, query.lang, query.page).bind()
                val items = shows.first.take(PAGE_SIZE)
                SearchResult(items, shows.first.size > PAGE_SIZE || shows.second > query.page)
            }
            null -> {
                val movies = searchMovies(query.query, query.lang, query.page).bind()
                val shows = searchTvShows(query.query, query.lang, query.page).bind()
                val interleaved = interleave(movies.first, shows.first)
                val items = interleaved.take(PAGE_SIZE)
                SearchResult(items, interleaved.size > PAGE_SIZE || movies.second > query.page || shows.second > query.page)
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 9
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

    private fun searchMovies(query: String, lang: Lang, page: Int): Either<SearchHandlerError, Pair<List<SearchResultItem>, Int>> =
        either {
            val (movies, totalPages) = tmdbClient.searchMovies(query, lang, page).bind()
            val items = movies.map {
                SearchResultItem(
                    tmdbId = it.id,
                    title = it.title,
                    overview = it.overview,
                    year = it.releaseDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                    posterPath = it.posterPath,
                    type = MediaType.movie.name,
                )
            }
            items to totalPages
        }

    private fun searchTvShows(query: String, lang: Lang, page: Int): Either<SearchHandlerError, Pair<List<SearchResultItem>, Int>> =
        either {
            val (tvshows, totalPages) = tmdbClient.searchTvShows(query, lang, page).bind()
            val items = tvshows.map {
                SearchResultItem(
                    tmdbId = it.id,
                    title = it.name,
                    overview = it.overview,
                    year = it.firstAirDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                    posterPath = it.posterPath,
                    type = MediaType.tvshow.name,
                )
            }
            items to totalPages
        }
}
