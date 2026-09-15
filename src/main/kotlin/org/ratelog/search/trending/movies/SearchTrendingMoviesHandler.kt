package org.ratelog.search.trending.movies

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.search.trending.SearchTrendingItem
import org.ratelog.tmdb.TmdbClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class SearchTrendingMoviesQuery(val lang: Lang)

data class SearchTrendingMovies(val items: List<SearchTrendingItem>)

@Service
class SearchTrendingMoviesHandler(
    private val tmdbClient: TmdbClient,
) {
    @Transactional
    fun handle(query: SearchTrendingMoviesQuery): Either<SearchTrendingMoviesHandlerError, SearchTrendingMovies> = either {
        SearchTrendingMovies(
            items = tmdbClient.trendingMovies(query.lang).bind()
                .take(LIMIT)
                .map {
                    SearchTrendingItem(
                        tmdbId = it.id,
                        title = it.title,
                        posterPath = it.posterPath,
                        year = it.releaseDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                    )
                }
        )
    }

    companion object {
        private const val LIMIT = 9
    }
}
