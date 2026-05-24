package org.raterr.tvshow

import org.raterr.TmdbId
import java.util.concurrent.atomic.AtomicLong

class InMemoryTvShowRepository : TvShowRepository {

    private val storage = mutableListOf<TvShow>()
    private val idGenerator = AtomicLong(1)

    fun clear() {
        storage.clear()
        idGenerator.set(1)
    }

    override fun findById(id: TvShow.Id): TvShow? =
        storage.firstOrNull { it.id == id }

    override fun findByTmdbId(tmdbId: TmdbId): TvShow? =
        storage.firstOrNull { it.tmdbId == tmdbId }

    override fun save(show: TvShow): TvShow =
        if (show.id == null) {
            val newShow = show.copy(id = idGenerator.getAndIncrement().let(TvShow::Id))
            storage.add(newShow)
            newShow
        } else {
            storage.removeIf { it.id == show.id }
            storage.add(show)
            show
        }
}
