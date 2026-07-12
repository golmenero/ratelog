package org.ratelog.test

import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.tvshow.TvDescription
import org.ratelog.tvshow.TvDescriptionRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryTvDescriptionRepository : TvDescriptionRepository {
    private val store = ConcurrentHashMap<Long, TvDescription>()
    private val idGenerator = java.util.concurrent.atomic.AtomicLong(1)

    override fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): TvDescription? =
        store.values.find { it.tmdbId == tmdbId && it.lang == lang }

    override fun findAllByTmdbId(tmdbId: TmdbId): List<TvDescription> =
        store.values.filter { it.tmdbId == tmdbId }

    override fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean =
        store.values.any { it.tmdbId == tmdbId }

    override fun saveAll(descriptions: List<TvDescription>) {
        descriptions.forEach {
            val desc = if (it.id == null) it.copy(id = idGenerator.getAndIncrement().let(TvDescription::Id)) else it
            store[desc.id!!.value] = desc
        }
    }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
