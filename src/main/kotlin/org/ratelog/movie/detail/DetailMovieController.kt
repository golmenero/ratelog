package org.ratelog.movie.detail

import org.ratelog.TmdbId
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
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
    val review: String?,
    val isFollowed: Boolean,
    val hasRating: Boolean,
)

@Controller
class DetailMovieController(
    private val handler: DetailMovieHandler,
) {

    @GetMapping("/movie/{id}")
    fun detailPage(
        @CurrentUser user: User,
        @PathVariable("id") tmdbId: Int,
        model: Model
    ): String =
        GetMovieDetail(
            userId = user.id!!,
            tmdbId = TmdbId(tmdbId),
            lang = user.lang,
        )
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "error.load.movie")
                    "movie-detail"
                },
                {
                    model.addAttribute("movie", buildResponse(it))
                    "movie-detail"
                }
            )

    private fun buildResponse(result: GetMovieDetailResult): MovieDetailResponse =
        MovieDetailResponse(
            id = result.id,
            tmdbId = result.tmdbId,
            name = result.title,
            overview = result.overview,
            firstAirYear = result.releaseYear,
            posterPath = result.posterPath,
            tmdbVoteAverage = result.tmdbVoteAverage,
            genres = result.genres,
            directing = result.directing,
            cinematography = result.cinematography,
            acting = result.acting,
            soundtrack = result.soundtrack,
            screenplay = result.screenplay,
            score = result.score,
            review = result.review,
            isFollowed = result.isFollowed,
            hasRating = result.isRated,
        )
}
