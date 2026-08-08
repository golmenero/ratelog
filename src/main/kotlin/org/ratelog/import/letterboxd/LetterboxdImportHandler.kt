package org.ratelog.import.letterboxd

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.TmdbId
import org.ratelog.movie.MovieRepository
import org.ratelog.movie.MovieDescriptionRepository
import org.ratelog.movie.rating.Rating
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tvshow.TvDescriptionRepository
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.Executors

data class ImportLetterboxdCommand(
    val userId: User.Id,
    val csvContent: ByteArray,
)

data class ImportLetterboxdResult(
    val importedMovies: Int,
    val importedTvShows: Int,
    val skippedDuplicates: Int,
    val notFound: List<String>,
    val errors: List<String>,
)

@Component
class LetterboxdImportHandler(
    private val csvParser: LetterboxdCsvParser,
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val movieDescriptionRepository: MovieDescriptionRepository,
    private val ratingRepository: RatingRepository,
    private val tvShowRepository: TvShowRepository,
    private val tvDescriptionRepository: TvDescriptionRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    private val concurrencyLimit = 10

    @Transactional
    fun handle(command: ImportLetterboxdCommand): Either<LetterboxdImportError, ImportLetterboxdResult> = either {
        val entries = csvParser.parse(command.csvContent.inputStream())
            .mapLeft { LetterboxdImportError.ParseError }
            .bind()

        val results = Executors.newFixedThreadPool(concurrencyLimit).use { executor ->
            val futures = entries.map { entry ->
                executor.submit<ProcessResult> {
                    try {
                        processEntry(entry, command.userId)
                    } catch (e: Exception) {
                        ProcessResult.Error(e.message ?: "Unknown error")
                    }
                }
            }
            futures.map { it.get() }
        }

        var importedMovies = 0
        var importedTvShows = 0
        var skippedDuplicates = 0
        val notFound = mutableListOf<String>()
        val errors = mutableListOf<String>()

        entries.forEachIndexed { index, entry ->
            when (val result = results[index]) {
                is ProcessResult.ImportedMovie -> importedMovies++
                is ProcessResult.ImportedTvShow -> importedTvShows++
                is ProcessResult.SkippedDuplicate -> skippedDuplicates++
                is ProcessResult.NotFound -> notFound.add(entry.name)
                is ProcessResult.Error -> errors.add("${entry.name}: ${result.message}")
            }
        }

        ImportLetterboxdResult(
            importedMovies = importedMovies,
            importedTvShows = importedTvShows,
            skippedDuplicates = skippedDuplicates,
            notFound = notFound,
            errors = errors,
        )
    }

    private fun processEntry(entry: LetterboxdEntry, userId: User.Id): ProcessResult {
        val ratingValue = entry.rating * 2.0
        if (ratingValue < 1.0 || ratingValue > 10.0) {
            return ProcessResult.Error("Rating out of range after conversion")
        }

        val score = Score(ratingValue)

        val searchResult = tmdbClient.searchMovies(entry.name, org.ratelog.Lang.en)
        if (searchResult.isRight()) {
            val movies = searchResult.getOrNull()?.first ?: emptyList()
            val matchingMovie = movies.find { movie ->
                movie.title.equals(entry.name, ignoreCase = true) ||
                (entry.year != null && movie.releaseDate?.take(4)?.toIntOrNull() == entry.year)
            } ?: movies.firstOrNull()

            if (matchingMovie != null) {
                val tmdbId = TmdbId(matchingMovie.id)
                val existingMovie = movieRepository.findByTmdbId(tmdbId)
                
                if (existingMovie != null) {
                    return processMovie(existingMovie, score, userId)
                }

                val movieDetails = tmdbClient.movieDetails(tmdbId)
                if (movieDetails.isRight()) {
                    val savedMovie = movieRepository.save(movieDetails.getOrNull()!!)

                    tmdbClient.movieTranslations(savedMovie.tmdbId, savedMovie.originalTitle).fold(
                        { },
                        { movieDescriptionRepository.saveAll(it) }
                    )

                    return processMovie(savedMovie, score, userId)
                }
            }
        }

        val tvSearchResult = tmdbClient.searchTvShows(entry.name, org.ratelog.Lang.en)
        if (tvSearchResult.isRight()) {
            val tvShows = tvSearchResult.getOrNull()?.first ?: emptyList()
            val matchingTvShow = tvShows.find { show ->
                show.name.equals(entry.name, ignoreCase = true) ||
                (entry.year != null && show.firstAirDate?.take(4)?.toIntOrNull() == entry.year)
            } ?: tvShows.firstOrNull()

            if (matchingTvShow != null) {
                val tmdbId = TmdbId(matchingTvShow.id)
                val existingTvShow = tvShowRepository.findByTmdbId(tmdbId)
                
                if (existingTvShow != null) {
                    return processTvShow(existingTvShow, score, userId)
                }

                val tvShowDetails = tmdbClient.tvShowDetails(tmdbId)
                if (tvShowDetails.isRight()) {
                    val savedTvShow = tvShowRepository.save(tvShowDetails.getOrNull()!!)

                    tmdbClient.tvTranslations(savedTvShow.tmdbId, savedTvShow.originalName).fold(
                        { },
                        { tvDescriptionRepository.saveAll(it) }
                    )

                    return processTvShow(savedTvShow, score, userId)
                }
            }
        }

        return ProcessResult.NotFound
    }

    private fun processMovie(movie: org.ratelog.movie.Movie, score: Score, userId: User.Id): ProcessResult {
        val existingRating = ratingRepository.findByMovieIdAndUserId(movie.id!!, userId)
        if (existingRating != null) {
            return ProcessResult.SkippedDuplicate
        }

        Rating(
            id = null,
            movieId = movie.id,
            userId = userId,
            directing = score,
            cinematography = score,
            acting = score,
            soundtrack = score,
            screenplay = score,
            createdAt = Instant.now(),
            review = null,
        ).updateScore().let(ratingRepository::save)

        return ProcessResult.ImportedMovie
    }

    private fun processTvShow(tvShow: org.ratelog.tvshow.TvShow, score: Score, userId: User.Id): ProcessResult {
        val existingRating = tvRatingRepository.findByTvShowIdAndUserId(tvShow.id!!, userId)
        if (existingRating != null) {
            return ProcessResult.SkippedDuplicate
        }

        val lastSeason = tvShow.lastSeasonNumber ?: 1
        var tvRating = TvRating.create(tvShow.id, userId, Instant.now())

        (1..lastSeason).forEach { seasonNumber ->
            tvRating = tvRating.addSeasonRating(
                seasonNumber = SeasonNumber(seasonNumber),
                directing = score,
                cinematography = score,
                acting = score,
                soundtrack = score,
                screenplay = score,
                createdAt = Instant.now(),
                review = null,
            )
        }

        tvRatingRepository.save(tvRating)

        return ProcessResult.ImportedTvShow
    }
}

sealed interface ProcessResult {
    data object ImportedMovie : ProcessResult
    data object ImportedTvShow : ProcessResult
    data object SkippedDuplicate : ProcessResult
    data object NotFound : ProcessResult
    data class Error(val message: String) : ProcessResult
}

sealed interface LetterboxdImportError {
    data object ParseError : LetterboxdImportError
}
