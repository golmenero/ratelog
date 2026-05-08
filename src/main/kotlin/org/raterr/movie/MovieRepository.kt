package org.raterr.movie

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MovieRepository : CrudRepository<Movie, Long> {
    fun findByTmdbId(tmdbId: Int): Optional<Movie>
}
