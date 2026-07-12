package org.ratelog.tvshow

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("tv")
data class TvShowEntity(
    @Id val id: Long? = null,
    @Column("tmdb_id") val tmdbId: Int,
    @Column("original_name") val originalName: String?,
    @Column("first_air_date") val firstAirDate: String?,
    @Column("first_air_year") val firstAirYear: Int?,
    @Column("poster_path") val posterPath: String?,
    @Column("tmdb_vote_average") val tmdbVoteAverage: Double?,
    val genres: String?,
    val status: String?,
    @Column("last_season_number") val lastSeasonNumber: Int?,
    @Column("last_season_air_date") val lastSeasonAirDate: String?,
    @Column("next_season_air_date") val nextSeasonAirDate: String?,
)

@Table("tv_follows")
data class TvFollowEntity(
    @Id val id: Long? = null,
    @Column("user_id") val userId: Long,
    @Column("tv_show_id") val tvShowId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Repository
interface TvShowDAO : CrudRepository<TvShowEntity, Long> {
    fun findByTmdbId(tmdbId: Int): Optional<TvShowEntity>

    @Query(
        """
        SELECT tv.* FROM tv tv
        INNER JOIN tv_follows tf ON tv.id = tf.tv_show_id
        WHERE tf.user_id = :userId
        """
    )
    fun findFollowedTvShows(userId: Long): List<TvShowEntity>

    @Query(
        """
        SELECT * FROM tv
        WHERE status IS NULL OR status NOT IN ('Ended', 'Canceled')
        """
    )
    fun findActiveTvShows(): List<TvShowEntity>
}

@Repository
interface TvFollowDAO : CrudRepository<TvFollowEntity, Long> {
    fun findByUserIdAndTvShowId(userId: Long, tvShowId: Long): Optional<TvFollowEntity>
}
