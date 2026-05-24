package org.raterr.tvshow

import org.raterr.Genre
import org.raterr.Overview
import org.raterr.TmdbId
import org.raterr.Title
import org.raterr.Url
import java.time.LocalDate

data class TvShow(
    val id: Id?,
    val tmdbId: TmdbId,
    val name: Title,
    val originalName: Title?,
    val overview: Overview?,
    val firstAirDate: LocalDate?,
    val firstAirYear: Int?,
    val posterPath: Url?,
    val tmdbVoteAverage: Double?,
    val genres: List<Genre>
) {
    data class Id(val value: Long)
}
