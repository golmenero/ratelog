package org.ratelog.tvshow

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Table("tv_descriptions")
data class TvDescriptionEntity(
    @Id @Column("tmdb_id") val tmdbId: Int,
    val lang: String,
    val name: String,
    val overview: String?,
)

@Repository
interface TvDescriptionDAO : CrudRepository<TvDescriptionEntity, Long> {

    @Query(
        """
        SELECT * FROM tv_descriptions
        WHERE tmdb_id = :tmdbId AND lang = :lang
        """
    )
    fun findByTmdbIdAndLang(tmdbId: Int, lang: String): TvDescriptionEntity?

    @Query(
        """
        SELECT * FROM tv_descriptions
        WHERE tmdb_id = :tmdbId
        """
    )
    fun findAllByTmdbId(tmdbId: Int): List<TvDescriptionEntity>

    @Query(
        """
        SELECT EXISTS(SELECT 1 FROM tv_descriptions WHERE tmdb_id = :tmdbId)
        """
    )
    fun existsAnyByTmdbId(tmdbId: Int): Boolean
}
