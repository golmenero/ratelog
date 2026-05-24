package org.raterr.tvshow

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("tv_shows")
data class TvShowEntity(
    @Id val id: Long? = null,
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

@Repository
interface TvShowDAO : CrudRepository<TvShowEntity, Long> {
    fun findByTmdbId(tmdbId: Int): Optional<TvShowEntity>
}
