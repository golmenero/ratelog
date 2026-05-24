package org.raterr.movie

import org.raterr.Genre
import org.raterr.Overview
import org.raterr.Title
import org.raterr.TmdbId
import org.raterr.Url
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

data class Movie(
    val id: Id?,
    val tmdbId: TmdbId,
    val title: Title,
    val originalTitle: Title?,
    val overview: Overview?,
    val releaseDate: LocalDate?,
    val releaseYear: Int?,
    val posterPath: Url?,
    val tmdbVoteAverage: Double?,
    val genres: List<Genre>
) {
    data class Id(val value: Long)
}

@Repository
interface MovieRepository {
    fun findById(id: Movie.Id): Movie?
    fun findByTmdbId(tmdbId: TmdbId): Movie?
    fun save(movie: Movie): Movie
}
