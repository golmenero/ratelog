package org.raterr.tvshow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tv_shows")
data class TvShow(
    @Id
    val id: Long? = null,
    @Column("tmdb_id")
    val tmdbId: Int,
    val name: String,
    @Column("original_name")
    val originalName: String? = null,
    val overview: String? = null,
    @Column("first_air_date")
    val firstAirDate: String? = null,
    @Column("first_air_year")
    val firstAirYear: Int? = null,
    @Column("poster_path")
    val posterPath: String? = null,
    @Column("tmdb_vote_average")
    val tmdbVoteAverage: Double? = null,
    val genres: String? = null
)
