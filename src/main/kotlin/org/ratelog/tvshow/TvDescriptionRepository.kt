package org.ratelog.tvshow

import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.TmdbId
import org.ratelog.Title

data class TvDescription(
    val tmdbId: TmdbId,
    val lang: Lang,
    val name: Title,
    val overview: Overview?,
)

interface TvDescriptionRepository {
    fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): TvDescription?
    fun findAllByTmdbId(tmdbId: TmdbId): List<TvDescription>
    fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean
    fun saveAll(descriptions: List<TvDescription>)
}
