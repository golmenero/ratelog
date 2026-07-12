package org.ratelog.test

import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.tvshow.TvDescription
import org.ratelog.tvshow.TvDescriptionRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryTvDescriptionRepository : TvDescriptionRepository {
    private val store = ConcurrentHashMap<Pair<Int, String>, TvDescription>()

    override fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): TvDescription? =
        store[Pair(tmdbId.value, lang.name)]

    override fun findAllByTmdbId(tmdbId: TmdbId): List<TvDescription> =
        store.values.filter { it.tmdbId == tmdbId }

    override fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean =
        store.keys.any { it.first == tmdbId.value }

    override fun saveAll(descriptions: List<TvDescription>) {
        descriptions.forEach {
            store[Pair(it.tmdbId.value, it.lang.name)] = it
        }
    }

    fun clear() {
        store.clear()
    }
}
