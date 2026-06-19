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
    private val follows = ConcurrentHashMap<Pair<Long, Long>, Boolean>()
    private val idGenerator = AtomicLong(1)

    override fun findById(id: TvShow.Id): TvShow? = store[id]

    override fun findByTmdbId(tmdbId: TmdbId): TvShow? =
        store.values.find { it.tmdbId == tmdbId }

    override fun save(show: TvShow): TvShow {
        val showToSave = if (show.id == null) {
            show.copy(id = TvShow.Id(idGenerator.getAndIncrement()))
        } else {
            show
        }
        store[showToSave.id!!] = showToSave
        return showToSave
    }

    override fun findFollowedTvShows(userId: User.Id): List<TvShow> =
        store.values.filter { follows[Pair(userId.value, it.id!!.value)] == true }

    override fun findActiveTvShows(): List<TvShow> =
        store.values.filter { it.status !in listOf(Status.ENDED, Status.CANCELED) }

    override fun isFollowed(userId: User.Id, showId: TvShow.Id): Boolean =
        follows[Pair(userId.value, showId.value)] == true

    override fun toggleFollow(userId: User.Id, showId: TvShow.Id) {
        val key = Pair(userId.value, showId.value)
        follows[key] = follows[key] != true
    }

    fun clear() {
        store.clear()
        follows.clear()
        idGenerator.set(1)
    }
}
