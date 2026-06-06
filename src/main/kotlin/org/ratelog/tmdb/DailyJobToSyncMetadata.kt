package org.ratelog.tmdb

import org.ratelog.movie.MovieRepository
import org.ratelog.tvshow.TvShowRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DailyJobToSyncMetadata(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val movieRepository: MovieRepository,
) {
    @Scheduled(cron = "0 0 6 * * *")
    fun sync() {
        syncTvShows()
        syncMovies()
    }

    private fun syncTvShows() {
        val showsToSync = tvShowRepository.findActiveTvShows()

        showsToSync.forEach { show ->
            tmdbClient.tvShowDetails(show.tmdbId.value)
                .fold({}, { tvShowRepository.save(it) })
        }
    }

    private fun syncMovies() {
        val moviesToSync = movieRepository.findActiveMovies()

        moviesToSync.forEach { movie ->
            tmdbClient.movieDetails(movie.tmdbId.value)
                .fold({}, { movieRepository.save(it) })
        }
    }
}
