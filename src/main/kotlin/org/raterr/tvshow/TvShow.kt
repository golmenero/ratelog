package org.raterr.tvshow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tv_shows")
data class TvShow(
    @Id val id: Long?,
    @Column("tmdb_id") val tmdbId: Int,
    val name: String,
    @Column("original_name") val originalName: String?,
    val overview: String?,
    @Column("first_air_date") val firstAirDate: String?,
    @Column("first_air_year") val firstAirYear: Int?,
    @Column("poster_path") val posterPath: String?,
    @Column("tmdb_vote_average") val tmdbVoteAverage: Double?,
    val genres: String?
)
