package org.ratelog.movie

import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.TmdbId
import org.ratelog.Title

data class MovieDescription(
    val tmdbId: TmdbId,
    val lang: Lang,
    val title: Title,
    val overview: Overview?,
)

interface MovieDescriptionRepository {
    fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): MovieDescription?
    fun findAllByTmdbId(tmdbId: TmdbId): List<MovieDescription>
    fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean
    fun saveAll(descriptions: List<MovieDescription>)
}
