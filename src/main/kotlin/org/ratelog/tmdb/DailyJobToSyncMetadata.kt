package org.ratelog.tmdb

import org.ratelog.Lang
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieRepository
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class DailyJobToSyncMetadata(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val movieRepository: MovieRepository,
) {
    private val logger = LoggerFactory.getLogger(DailyJobToSyncMetadata::class.java)

    private val concurrencyLimit = 10

    @Scheduled(cron = "0 0 6 * * *")
    fun sync() {
        logger.info("DailyJobToSyncMetadata started")
        try {
            Executors.newFixedThreadPool(concurrencyLimit).use { executor ->
                syncTvShows(executor)
                syncMovies(executor)
            }
            logger.info("DailyJobToSyncMetadata completed successfully")
        } catch (e: Exception) {
            logger.error("DailyJobToSyncMetadata failed", e)
        }
    }

    private fun syncTvShows(executor: ExecutorService) {
        val showsToSync = tvShowRepository.findActiveTvShows()

        val futures = showsToSync.map { show ->
            executor.submit { syncTvShow(show) }
        }

        futures.forEach { it.get() }
    }

    private fun syncMovies(executor: ExecutorService) {
        val moviesToSync = movieRepository.findActiveMovies()

        val futures = moviesToSync.map { movie ->
            executor.submit { syncMovie(movie) }
        }

        futures.forEach { it.get() }
    }

    private fun syncTvShow(show: TvShow) {
        try {
            tmdbClient.tvShowDetails(show.tmdbId)
                .fold(
                    { err -> logger.error("Failed to sync TV show: $err") },
                    { it.copy(id = show.id).let(tvShowRepository::save) }
                )
        } catch (e: Exception) {
            logger.error("Error syncing TV show", e)
        }
    }

    private fun syncMovie(movie: Movie) {
        try {
            tmdbClient.movieDetails(movie.tmdbId)
                .fold(
                    { err -> logger.error("Failed to sync movie: $err") },
                    { it.copy(id = movie.id).let(movieRepository::save) }
                )
        } catch (e: Exception) {
            logger.error("Error syncing movie", e)
        }
    }
}
