package org.ratelog.search.trending

import org.ratelog.MediaType

data class SearchTrendingItem(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val year: Int?,
    val type: MediaType,
)
