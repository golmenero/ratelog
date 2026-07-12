package org.ratelog.test

import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.movie.MovieDescription
import org.ratelog.movie.MovieDescriptionRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryMovieDescriptionRepository : MovieDescriptionRepository {
    private val store = ConcurrentHashMap<Long, MovieDescription>()
    private val idGenerator = java.util.concurrent.atomic.AtomicLong(1)

    override fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): MovieDescription? =
        store.values.find { it.tmdbId == tmdbId && it.lang == lang }

    override fun findAllByTmdbId(tmdbId: TmdbId): List<MovieDescription> =
        store.values.filter { it.tmdbId == tmdbId }

    override fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean =
        store.values.any { it.tmdbId == tmdbId }

    override fun saveAll(descriptions: List<MovieDescription>) {
        descriptions.forEach {
            val desc = if (it.id == null) it.copy(id = idGenerator.getAndIncrement().let(MovieDescription::Id)) else it
            store[desc.id!!.value] = desc
        }
    }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
