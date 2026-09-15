package org.ratelog.search.trending.tvshows

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.search.trending.SearchTrendingItem
import org.ratelog.tmdb.TmdbClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class SearchTrendingTvShowsQuery(val lang: Lang)

data class SearchTrendingTvShows(val items: List<SearchTrendingItem>)

@Service
class SearchTrendingTvShowsHandler(
    private val tmdbClient: TmdbClient,
) {
    @Transactional
    fun handle(query: SearchTrendingTvShowsQuery): Either<SearchTrendingTvShowsHandlerError, SearchTrendingTvShows> = either {
        SearchTrendingTvShows(
            items = tmdbClient.trendingTvShows(query.lang).bind()
                .take(LIMIT)
                .map {
                    SearchTrendingItem(
                        tmdbId = it.id,
                        title = it.name,
                        posterPath = it.posterPath,
                        year = it.firstAirDate?.takeIf { d -> d.isNotBlank() }?.take(4)?.toIntOrNull(),
                    )
                }
        )
    }

    companion object {
        private const val LIMIT = 9
    }
}
