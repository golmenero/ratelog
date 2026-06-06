package org.ratelog.tmdb

import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.Status
import org.ratelog.Title
import org.ratelog.Url
import org.ratelog.movie.MovieRepository
import org.ratelog.tvshow.TvShowRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DailyJobToSyncMetadata(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val movieRepository: MovieRepository,
) {
//    @Scheduled(cron = "0 * * * * *")
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
