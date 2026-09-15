package org.ratelog.search.trending

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.MediaType
import org.ratelog.tmdb.TmdbClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class SearchTrendingQuery(val lang: Lang)

data class SearchTrending(val items: List<SearchTrendingItem>)

@Service
class SearchTrendingHandler(
    private val tmdbClient: TmdbClient,
) {
    @Transactional
    fun handle(query: SearchTrendingQuery): Either<SearchTrendingHandlerError, SearchTrending> = either {
        val movies = tmdbClient.trendingMovies(query.lang).bind().take(LIMIT).map {
            SearchTrendingItem(
                tmdbId = it.id,
                title = it.title,
                posterPath = it.posterPath,
                year = it.releaseDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                type = MediaType.movie,
            )
        }
        val tvShows = tmdbClient.trendingTvShows(query.lang).bind().take(LIMIT).map {
            SearchTrendingItem(
                tmdbId = it.id,
                title = it.name,
                posterPath = it.posterPath,
                year = it.firstAirDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                type = MediaType.tvshow,
            )
        }
        SearchTrending(items = interleave(movies, tvShows))
    }

    companion object {
        private const val LIMIT = 8

        private fun interleave(movies: List<SearchTrendingItem>, tvShows: List<SearchTrendingItem>): List<SearchTrendingItem> {
            val result = mutableListOf<SearchTrendingItem>()
            val maxSize = maxOf(movies.size, tvShows.size)
            for (i in 0 until maxSize) {
                if (i < movies.size) result.add(movies[i])
                if (i < tvShows.size) result.add(tvShows[i])
            }
            return result
        }
    }
}
