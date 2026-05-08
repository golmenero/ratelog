package org.raterr.movie

import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class GetTopMoviesController(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val userService: UserService
) {

    @GetMapping("/movie/top")
    fun topsPage(
        @RequestParam("limit", required = false) limit: Int?,
        @RequestParam("year", required = false) year: Int?,
        @RequestParam("category", required = false) category: String?,
        model: Model
    ): String {
        try {
            val user = userService.getCurrentUser()

            if (user == null) {
                model.addAttribute("tops", emptyList<GetTopMoviesResponse>())
                model.addAttribute("selectedYear", year)
                model.addAttribute("selectedCategory", category)
                model.addAttribute("availableCategories", emptyList<String>())
                return "top"
            }

            val ratings = ratingRepository.findByUserId(user.id!!)
            val movieIds = ratings.map { it.movieId }.toSet()
            val movies = movieRepository.findAllById(movieIds)
            val moviesById = movies.associateBy { it.id!! }
            val tmdbIdByMovieId = movies.associate { it.id!! to it.tmdbId }
            val tops = getTopMovies(ratings, moviesById, tmdbIdByMovieId, limit, year, category)

            model.addAttribute("tops", tops)
            model.addAttribute("selectedYear", year)
            model.addAttribute("selectedCategory", category)
            return "top"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the tops.")
            return "top"
        }
    }

    private fun getTopMovies(
        ratings: List<Rating>,
        moviesById: Map<Long, Movie>,
        tmdbIdByMovieId: Map<Long, Int>,
        limit: Int?,
        year: Int?,
        category: String?
    ): List<GetTopMoviesResponse> {
        val safeLimit = limit?.coerceIn(1, 100)

        var filtered = if (year != null) {
            ratings.filter { moviesById[it.movieId]?.releaseYear == year }
        } else {
            ratings
        }

        if (category != null && category.isNotBlank()) {
            filtered = filtered.filter { rating ->
                moviesById[rating.movieId]?.genres?.contains(category, ignoreCase = true) == true
            }
        }

        val results = filtered
            .groupBy { it.movieId }
            .map { (movieId, ratingsForMovie) ->
                val movie = moviesById[movieId]
                val tmdbId = tmdbIdByMovieId[movieId] ?: 0

                if (movie == null) {
                    return@map GetTopMoviesResponse(
                        tmdbId = tmdbId,
                        title = "Unknown",
                        releaseYear = null,
                        posterPath = null,
                        averageScore = 0.0,
                        ratingsCount = ratingsForMovie.size
                    )
                }

                val count = ratingsForMovie.size

                val individualScores = ratingsForMovie.map { rating ->
                    (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
                }

                val avgScore = if (individualScores.isNotEmpty()) individualScores.average() else 0.0
                val avgDirecting = if (ratingsForMovie.isNotEmpty()) ratingsForMovie.map { it.directing }.average() else 0.0
                val avgCinematography = if (ratingsForMovie.isNotEmpty()) ratingsForMovie.map { it.cinematography }.average() else 0.0
                val avgActing = if (ratingsForMovie.isNotEmpty()) ratingsForMovie.map { it.acting }.average() else 0.0
                val avgSoundtrack = if (ratingsForMovie.isNotEmpty()) ratingsForMovie.map { it.soundtrack }.average() else 0.0
                val avgScreenplay = if (ratingsForMovie.isNotEmpty()) ratingsForMovie.map { it.screenplay }.average() else 0.0

                GetTopMoviesResponse(
                    tmdbId = movie.tmdbId,
                    title = movie.title,
                    releaseYear = movie.releaseYear,
                    posterPath = movie.posterPath,
                    averageScore = avgScore,
                    ratingsCount = count,
                    directing = avgDirecting,
                    cinematography = avgCinematography,
                    acting = avgActing,
                    soundtrack = avgSoundtrack,
                    screenplay = avgScreenplay
                )
            }
            .sortedByDescending { it.averageScore }

        return if (safeLimit != null) results.take(safeLimit) else results
    }
}

data class GetTopMoviesResponse(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?,
    val posterPath: String?,
    val averageScore: Double,
    val ratingsCount: Int,
    val directing: Double = 0.0,
    val cinematography: Double = 0.0,
    val acting: Double = 0.0,
    val soundtrack: Double = 0.0,
    val screenplay: Double = 0.0
)
