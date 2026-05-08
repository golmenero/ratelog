package org.raterr.movie

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("movies")
data class Movie(
    @Id
    val id: Long? = null,
    @Column("tmdb_id")
    val tmdbId: Int,
    val title: String,
    @Column("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    @Column("release_date")
    val releaseDate: String? = null,
    @Column("release_year")
    val releaseYear: Int? = null,
    @Column("poster_path")
    val posterPath: String? = null,
    @Column("tmdb_vote_average")
    val tmdbVoteAverage: Double? = null,
    val genres: String? = null
)
