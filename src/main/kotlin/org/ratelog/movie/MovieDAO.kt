package org.ratelog.movie

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("movies")
data class MovieEntity(
    @Id val id: Long? = null,
    @Column("tmdb_id") val tmdbId: Int,
    @Column("original_title") val originalTitle: String?,
    @Column("release_date") val releaseDate: String?,
    @Column("release_year") val releaseYear: Int?,
    @Column("poster_path") val posterPath: String?,
    @Column("tmdb_vote_average") val tmdbVoteAverage: Double?,
    val genres: String?,
    val status: String?,
)

@Table("movie_follows")
data class MovieFollowEntity(
    @Id val id: Long? = null,
    @Column("user_id") val userId: Long,
    @Column("movie_id") val movieId: Long,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Repository
interface MovieDAO : CrudRepository<MovieEntity, Long> {
    fun findByTmdbId(tmdbId: Int): Optional<MovieEntity>

    @Query(
        """
        SELECT m.* FROM movies m
        INNER JOIN movie_follows mf ON m.id = mf.movie_id
        WHERE mf.user_id = :userId
        """
    )
    fun findFollowedMovies(userId: Long): List<MovieEntity>

    @Query(
        """
        SELECT * FROM movies
        WHERE status IS NULL OR status NOT IN ('Released', 'Canceled')
        """
    )
    fun findActiveMovies(): List<MovieEntity>
}

@Repository
interface MovieFollowDAO : CrudRepository<MovieFollowEntity, Long> {
    fun findByUserIdAndMovieId(userId: Long, movieId: Long): Optional<MovieFollowEntity>
}
