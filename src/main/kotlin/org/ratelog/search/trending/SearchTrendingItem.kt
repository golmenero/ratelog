package org.ratelog.search.trending

data class SearchTrendingItem(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val year: Int?,
)
