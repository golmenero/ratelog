package org.raterr.movie

import org.raterr.rating.RatingScoreService
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.jvm.optionals.getOrNull

data class GetTopMoviesResponse(
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?,
    val posterPath: String?,
    val averageScore: Double,
    val directing: Double = 0.0,
    val cinematography: Double = 0.0,
    val acting: Double = 0.0,
    val soundtrack: Double = 0.0,
    val screenplay: Double = 0.0
)

@Controller
class GetTopMoviesController(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {

    @GetMapping("/movie/top")
    fun topsPage(
        @CurrentUser user: User,
        @RequestParam("year", required = false) year: Int?,
        @RequestParam("category", required = false) category: String?,
        model: Model
    ): String {
        try {
            val tops = ratingRepository.findByUserIdWithFilters(user.id!!, year, category)
                .map { it to  it.movieId.let(movieRepository::findById).getOrNull()!! }
                .top()

            model.addAttribute("tops", tops)
            model.addAttribute("selectedYear", year)
            model.addAttribute("selectedCategory", category)
            return "top"
        } catch (e: Exception) {
            model.addAttribute("error", "Could not load the tops.")
            return "top"
        }
    }

    private fun List<Pair<Rating, Movie>>.top(): List<GetTopMoviesResponse> =
        sortedByDescending { RatingScoreService.score(it.first) }
            .map {
                GetTopMoviesResponse(
                    tmdbId = it.second.tmdbId,
                    title = it.second.title,
                    releaseYear = it.second.releaseYear,
                    posterPath = it.second.posterPath,
                    averageScore = RatingScoreService.score(it.first),
                    directing = it.first.directing,
                    cinematography = it.first.cinematography,
                    acting = it.first.acting,
                    soundtrack = it.first.soundtrack,
                    screenplay = it.first.screenplay
                )
            }
}
