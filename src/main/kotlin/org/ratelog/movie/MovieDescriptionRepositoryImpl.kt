package org.ratelog.movie

import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.TmdbId
import org.ratelog.Title
import org.springframework.stereotype.Repository

@Repository
class MovieDescriptionRepositoryImpl(
    private val movieDescriptionDAO: MovieDescriptionDAO,
) : MovieDescriptionRepository {

    override fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): MovieDescription? =
        movieDescriptionDAO.findByTmdbIdAndLang(tmdbId.value, lang.name)?.toDomain()

    override fun findAllByTmdbId(tmdbId: TmdbId): List<MovieDescription> =
        movieDescriptionDAO.findAllByTmdbId(tmdbId.value).map { it.toDomain() }

    override fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean =
        movieDescriptionDAO.existsAnyByTmdbId(tmdbId.value)

    override fun saveAll(descriptions: List<MovieDescription>) {
        val entities = descriptions.map { it.toEntity() }
        movieDescriptionDAO.saveAll(entities)
    }

    private fun MovieDescriptionEntity.toDomain(): MovieDescription =
        MovieDescription(
            id = id?.let(MovieDescription::Id),
            tmdbId = TmdbId(tmdbId),
            lang = Lang.parse(lang),
            title = Title(title),
            overview = overview?.let { Overview(it) },
        )

    private fun MovieDescription.toEntity(): MovieDescriptionEntity =
        MovieDescriptionEntity(
            id = id?.value,
            tmdbId = tmdbId.value,
            lang = lang.name,
            title = title.value,
            overview = overview?.value,
        )
}
