package org.ratelog.tmdb

import org.ratelog.Genre
import org.ratelog.Overview
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
        val allShows = tvShowRepository.findAll()

        val showsToSync = allShows.filter { show ->
            show.status !in listOf("Ended", "Canceled")
        }

        showsToSync.forEach { show ->
            tmdbClient.tvShowDetails(show.tmdbId.value).fold(
                {},
                { tmdbShow ->
                    val seasons = tmdbShow.seasons.filter { it.seasonNumber > 0 }
                    val today = LocalDate.now()

                    val latestSeasonWithDate = seasons
                        .filter { !it.airDate.isNullOrBlank() }
                        .maxByOrNull { it.seasonNumber }

                    val nextSeasonWithDate = seasons
                        .filter { !it.airDate.isNullOrBlank() }
                        .mapNotNull { s -> s.airDate?.let { d -> s to LocalDate.parse(d) } }
                        .filter { (_, date) -> date > today }
                        .minByOrNull { (_, date) -> date }
                        ?.first

                    val lastSeasonNumber = latestSeasonWithDate?.seasonNumber
                    val lastSeasonAirDate = latestSeasonWithDate?.airDate?.let { LocalDate.parse(it) }
                    val nextSeasonAirDate = nextSeasonWithDate?.airDate?.let { LocalDate.parse(it) }

                    val updated = show.copy(
                        name = Title(tmdbShow.name),
                        originalName = tmdbShow.originalName?.let { Title(it) },
                        overview = tmdbShow.overview?.let { Overview(it) },
                        firstAirDate = tmdbShow.firstAirDate?.let { LocalDate.parse(it) },
                        firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                        posterPath = tmdbShow.posterPath?.let { Url(it) },
                        tmdbVoteAverage = tmdbShow.voteAverage,
                        genres = tmdbShow.genres.mapNotNull { Genre.fromValue(it.name) },
                        status = tmdbShow.status,
                        lastSeasonNumber = lastSeasonNumber,
                        lastSeasonAirDate = lastSeasonAirDate,
                        nextSeasonAirDate = nextSeasonAirDate,
                    )

                    tvShowRepository.save(updated)
                }
            )
        }
    }

    private fun syncMovies() {
        val allMovies = movieRepository.findAll()

        val moviesToSync = allMovies.filter { movie ->
            movie.status != "Released"
        }

        moviesToSync.forEach { movie ->
            tmdbClient.movieDetails(movie.tmdbId.value).fold(
                {},
                { tmdbMovie ->
                    val updated = movie.copy(
                        title = Title(tmdbMovie.title),
                        originalTitle = tmdbMovie.originalTitle?.let { Title(it) },
                        overview = tmdbMovie.overview?.let { Overview(it) },
                        releaseDate = tmdbMovie.releaseDate?.let { LocalDate.parse(it) },
                        releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                        posterPath = tmdbMovie.posterPath?.let { Url(it) },
                        tmdbVoteAverage = tmdbMovie.voteAverage,
                        genres = tmdbMovie.genres.mapNotNull { Genre.fromValue(it.name) },
                        status = tmdbMovie.status,
                    )

                    movieRepository.save(updated)
                }
            )
        }
    }
}
