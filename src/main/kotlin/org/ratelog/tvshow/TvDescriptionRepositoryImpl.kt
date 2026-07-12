package org.ratelog.tvshow

import org.ratelog.tvshow.TvDescription
import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.TmdbId
import org.ratelog.Title
import org.springframework.stereotype.Repository

@Repository
class TvDescriptionRepositoryImpl(
    private val tvDescriptionDAO: TvDescriptionDAO,
) : TvDescriptionRepository {

    override fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): TvDescription? =
        tvDescriptionDAO.findByTmdbIdAndLang(tmdbId.value, lang.name)?.toDomain()

    override fun findAllByTmdbId(tmdbId: TmdbId): List<TvDescription> =
        tvDescriptionDAO.findAllByTmdbId(tmdbId.value).map { it.toDomain() }

    override fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean =
        tvDescriptionDAO.existsAnyByTmdbId(tmdbId.value)

    override fun saveAll(descriptions: List<TvDescription>) {
        descriptions.map { it.toEntity() }.forEach { tvDescriptionDAO.save(it) }
    }

    private fun TvDescriptionEntity.toDomain(): TvDescription =
        TvDescription(
            tmdbId = TmdbId(tmdbId),
            lang = Lang.parse(lang),
            name = Title(name),
            overview = overview?.let { Overview(it) },
        )

    private fun TvDescription.toEntity(): TvDescriptionEntity =
        TvDescriptionEntity(
            tmdbId = tmdbId.value,
            lang = lang.name,
            name = name.value,
            overview = overview?.value,
        )
}
