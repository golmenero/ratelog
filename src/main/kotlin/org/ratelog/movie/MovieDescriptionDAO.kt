package org.ratelog.movie

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Table("movie_descriptions")
data class MovieDescriptionEntity(
    @Id val id: Long? = null,
    @Column("tmdb_id") val tmdbId: Int,
    val lang: String,
    val title: String,
    val overview: String?,
)

@Repository
interface MovieDescriptionDAO : CrudRepository<MovieDescriptionEntity, Long> {

    @Query(
        """
        SELECT * FROM movie_descriptions
        WHERE tmdb_id = :tmdbId AND lang = :lang
        """
    )
    fun findByTmdbIdAndLang(tmdbId: Int, lang: String): MovieDescriptionEntity?

    @Query(
        """
        SELECT * FROM movie_descriptions
        WHERE tmdb_id = :tmdbId
        """
    )
    fun findAllByTmdbId(tmdbId: Int): List<MovieDescriptionEntity>

    @Query(
        """
        SELECT EXISTS(SELECT 1 FROM movie_descriptions WHERE tmdb_id = :tmdbId)
        """
    )
    fun existsAnyByTmdbId(tmdbId: Int): Boolean
}
