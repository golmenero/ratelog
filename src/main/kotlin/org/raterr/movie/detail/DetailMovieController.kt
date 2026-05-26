package org.raterr.movie.detail

import org.raterr.TmdbId
import org.raterr.movie.rating.RatingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

data class MovieDetailResponse(
    val id: Long,
    val tmdbId: Int,
    val name: String,
    val overview: String?,
    val firstAirYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val genres: List<String>,
    val directing: Double?,
    val cinematography: Double?,
    val acting: Double?,
    val soundtrack: Double?,
    val screenplay: Double?,
    val score: Double?,
    val isFollowed: Boolean,
    val hasRating: Boolean,
)

@Controller
class MovieDetailController(
    private val handler: GetMovieDetailHandler,
    private val ratingRepository: RatingRepository,
) {

    @GetMapping("/movie/{id}")
    fun detailPage(
        @PathVariable("id") tmdbId: Int,
        model: Model
    ): String =
        GetMovieDetail(tmdbId = TmdbId(tmdbId))
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load the movie.")
                    "movie-detail"
                },
                {
                    model.addAttribute("movie", buildResponse(it))
                    "movie-detail"
                }
            )

    private fun buildResponse(result: GetMovieDetailResult): MovieDetailResponse =
        MovieDetailResponse(
            id = result.movie.id!!.value,
            tmdbId = result.movie.tmdbId.value,
            name = result.movie.title.value,
            overview = result.movie.overview?.value,
            firstAirYear = result.movie.releaseYear,
            posterPath = result.movie.posterPath?.value,
            tmdbVoteAverage = result.movie.tmdbVoteAverage,
            genres = result.movie.genres.map { it.value },
            directing = result.directing,
            cinematography = result.cinematography,
            acting = result.acting,
            soundtrack = result.soundtrack,
            screenplay = result.screenplay,
            score = result.score,
            isFollowed = result.movie.followed,
            hasRating = result.movie.id.let { ratingRepository.findFirstByMovieId(it) } != null,
        )
}
