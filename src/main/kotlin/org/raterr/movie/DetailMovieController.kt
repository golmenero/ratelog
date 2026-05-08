package org.raterr.movie

import org.raterr.TmdbClient
import org.raterr.TmdbMovie
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class GetMovieDetailsController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository
) {

    @GetMapping("/movie/rate")
    fun ratePage(@RequestParam("id") tmdbId: Int, model: Model): String {
        try {
            val movie = getOrFetchMovie(tmdbId)
            val ratings = ratingRepository.findByMovieId(movie.id!!)
            val alreadyRated = ratings.isNotEmpty()

            model.addAttribute("movie", buildResponse(movie, ratings))
            model.addAttribute("alreadyRated", alreadyRated)
            return "rate"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the movie.")
            return "rate"
        }
    }

    private fun getOrFetchMovie(tmdbId: Int): Movie {
        val localMovie = movieRepository.findByTmdbId(tmdbId).orElse(null)
        return localMovie ?: upsertMovie(tmdbClient.movieDetails(tmdbId))
    }

    private fun upsertMovie(tmdbMovie: TmdbMovie): Movie {
        val existing = movieRepository.findByTmdbId(tmdbMovie.id).orElse(null)
        val genres = tmdbMovie.genres.joinToString(",") { it.name }

        return if (existing != null) {
            val updated = existing.copy(
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )
            movieRepository.save(updated)
        } else {
            val newMovie = Movie(
                tmdbId = tmdbMovie.id,
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )
            movieRepository.save(newMovie)
        }
    }

    private fun buildResponse(movie: Movie, ratings: List<Rating>): GetMovieDetailsResponse {
        val stats = calculateStats(ratings)

        return GetMovieDetailsResponse(
            tmdbId = movie.tmdbId,
            title = movie.title,
            overview = movie.overview,
            releaseDate = movie.releaseDate,
            releaseYear = movie.releaseYear,
            posterPath = movie.posterPath,
            tmdbVoteAverage = movie.tmdbVoteAverage,
            averageScore = stats.averageScore,
            ratingsCount = stats.ratingsCount
        )
    }

    private fun calculateStats(ratings: List<Rating>): GetMovieScoreStats {
        if (ratings.isEmpty()) {
            return GetMovieScoreStats(averageScore = 0.0, ratingsCount = 0)
        }

        val avg = ratings.map { rating ->
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
        }.average()

        return GetMovieScoreStats(averageScore = avg, ratingsCount = ratings.size)
    }
}

private data class GetMovieScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class GetMovieDetailsResponse(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val releaseYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val averageScore: Double,
    val ratingsCount: Int
)
