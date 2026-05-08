package org.raterr.tvshow

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TvShowRepository : CrudRepository<TvShow, Long> {
    fun findByTmdbId(tmdbId: Int): Optional<TvShow>
}
