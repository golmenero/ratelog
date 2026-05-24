package org.raterr.tvshow

import org.raterr.TmdbId

interface TvShowRepository {
    fun findById(id: TvShow.Id): TvShow?
    fun findByTmdbId(tmdbId: TmdbId): TvShow?
    fun save(show: TvShow): TvShow
}
