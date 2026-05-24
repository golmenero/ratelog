package org.raterr.movie

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("movies")
data class Movie(
    @Id val id: Long?,
    @Column("tmdb_id") val tmdbId: Int,
    val title: String,
    @Column("original_title") val originalTitle: String?,
    val overview: String?,
    @Column("release_date") val releaseDate: String?,
    @Column("release_year") val releaseYear: Int?,
    @Column("poster_path") val posterPath: String?,
    @Column("tmdb_vote_average") val tmdbVoteAverage: Double?,
    val genres: String?
)
