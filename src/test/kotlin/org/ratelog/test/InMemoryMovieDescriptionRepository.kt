package org.ratelog.test

import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.movie.MovieDescription
import org.ratelog.movie.MovieDescriptionRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryMovieDescriptionRepository : MovieDescriptionRepository {
    private val store = ConcurrentHashMap<Pair<Int, String>, MovieDescription>()

    override fun findByTmdbIdAndLang(tmdbId: TmdbId, lang: Lang): MovieDescription? =
        store[Pair(tmdbId.value, lang.name)]

    override fun findAllByTmdbId(tmdbId: TmdbId): List<MovieDescription> =
        store.values.filter { it.tmdbId == tmdbId }

    override fun existsAnyByTmdbId(tmdbId: TmdbId): Boolean =
        store.keys.any { it.first == tmdbId.value }

    override fun saveAll(descriptions: List<MovieDescription>) {
        descriptions.forEach {
            store[Pair(it.tmdbId.value, it.lang.name)] = it
        }
    }

    fun clear() {
        store.clear()
    }
}
