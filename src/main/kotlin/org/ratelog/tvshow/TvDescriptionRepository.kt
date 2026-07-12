package org.ratelog.tvshow

import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.TmdbId
import org.ratelog.Title

data class TvDescription(
    val id: Id?,
    val tmdbId: TmdbId,
    val lang: Lang,
    val name: Title,
    val overview: Overview?,
)  {
    data class Id(val value: Long)
}

interface TvDescriptionRepository {
    fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): TvDescription?
    fun findAllByTmdbId(tmdbId: TmdbId): List<TvDescription>
    fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean
    fun saveAll(descriptions: List<TvDescription>)
}
