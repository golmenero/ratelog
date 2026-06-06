package org.ratelog.tmdb

import org.ratelog.movie.MovieRepository
import org.ratelog.tvshow.TvShowRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DailyJobToSyncMetadata(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val movieRepository: MovieRepository,
) {
    private val logger = LoggerFactory.getLogger(DailyJobToSyncMetadata::class.java)

    @Scheduled(cron = "0 0 6 * * *")
    fun sync() {
        logger.info("DailyJobToSyncMetadata started")
        try {
            syncTvShows()
            syncMovies()
            logger.info("DailyJobToSyncMetadata completed successfully")
        } catch (e: Exception) {
            logger.error("DailyJobToSyncMetadata failed", e)
        }
    }

    private fun syncTvShows() {
        logger.info("Syncing TV shows")
        val showsToSync = tvShowRepository.findActiveTvShows()
        logger.info("Found ${showsToSync.size} TV shows to sync")

        showsToSync.forEach { show ->
            try {
                tmdbClient.tvShowDetails(show.tmdbId.value)
                    .fold(
                        { err -> logger.error("Failed to sync TV show ${show.tmdbId.value}: $err") },
                        { it.copy(id = show.id).let(tvShowRepository::save) }
                    )
            } catch (e: Exception) {
                logger.error("Error syncing TV show ${show.tmdbId.value}", e)
            }
        }
    }

    private fun syncMovies() {
        logger.info("Syncing movies")
        val moviesToSync = movieRepository.findActiveMovies()
        logger.info("Found ${moviesToSync.size} movies to sync")

        moviesToSync.forEach { movie ->
            try {
                tmdbClient.movieDetails(movie.tmdbId.value)
                    .fold(
                        { err -> logger.error("Failed to sync movie ${movie.tmdbId.value}: $err") },
                        { it.copy(id = movie.id).let(movieRepository::save) }
                    )
            } catch (e: Exception) {
                logger.error("Error syncing movie ${movie.tmdbId.value}", e)
            }
        }
    }
}
