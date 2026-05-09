package org.raterr.movie

import org.raterr.TmdbClient
import org.raterr.TmdbMovie
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class GetMovieDetailsResponse(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val releaseYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
)

@Controller
class GetMovieDetailsController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
) {

    @GetMapping("/movie/rate")
    fun ratePage(@RequestParam("id") tmdbId: Int, model: Model): String {
        try {
            val movie = getOrCreateMovie(tmdbId)
            model.addAttribute("movie", buildResponse(movie))
            return "rate"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the movie.")
            return "rate"
        }
    }

    private fun getOrCreateMovie(tmdbId: Int): Movie {
        val tmdbMovie = tmdbClient.movieDetails(tmdbId)
        val genres = tmdbMovie.genres.joinToString(",") { it.name }

        val movie = tmdbId
            .let(movieRepository::findByTmdbId)
            .orElse(null)
            ?.copy(
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = genres
            )
            ?: Movie(
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

        return movie.let(movieRepository::save)
    }

    private fun buildResponse(movie: Movie): GetMovieDetailsResponse =
        GetMovieDetailsResponse(
            tmdbId = movie.tmdbId,
            title = movie.title,
            overview = movie.overview,
            releaseDate = movie.releaseDate,
            releaseYear = movie.releaseYear,
            posterPath = movie.posterPath,
            tmdbVoteAverage = movie.tmdbVoteAverage,
        )
}
