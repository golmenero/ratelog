package org.raterr.movie.get

import org.raterr.TmdbId
import org.raterr.movie.Movie
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
class GetMovieController(
    private val handler: GetMovieHandler,
) {

    @GetMapping("/movie/rate")
    fun ratePage(
        @RequestParam("id") tmdbId: Int,
        model: Model
    ): String =
        GetMovie(tmdbId = tmdbId.let(::TmdbId))
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load the movie.")
                    return "rate"
                },
                {
                    model.addAttribute("movie", buildResponse(it))
                    return "rate"
                },
            )

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
