package org.ratelog.test

import org.ratelog.*
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.user.User
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class InMemoryTvShowRepository : TvShowRepository {
    private val store = ConcurrentHashMap<TvShow.Id, TvShow>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: TvShow.Id): TvShow? = store[id]

    override fun findByTmdbId(tmdbId: TmdbId): TvShow? =
        store.values.find { it.tmdbId == tmdbId }

    override fun save(show: TvShow) {
        val showToSave = if (show.id == null) {
            show.copy(id = TvShow.Id(idGenerator.getAndIncrement()))
        } else {
            show
        }
        store[showToSave.id!!] = showToSave
    }

    override fun findFollowedTvShows(userId: User.Id): List<TvShow> =
        store.values.filter { it.followed }

    fun clear() {
        store.clear()
        idGenerator.set(1)
    }
}
