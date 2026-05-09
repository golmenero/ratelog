package org.raterr.movie.top

import org.raterr.UserId
import org.raterr.rating.RatingScoreService
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.annotations.CurrentUser
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
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
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double
)

@Controller
class TopMovieController(
    private val handler: TopMovieHandler,
) {

    @GetMapping("/movie/top")
    fun topsPage(
        @CurrentUser user: User,
        @RequestParam("year", required = false) year: Int?,
        @RequestParam("category", required = false) category: String?,
        model: Model
    ): String {
        val tops = TopMovie(
            userId = user.id!!.let(::UserId),
            year = year,
            category = category
        ).let(handler::handle)

        model.addAttribute("tops", tops.let(::map))
        model.addAttribute("selectedYear", year)
        model.addAttribute("selectedCategory", category)

        return "top"
    }

    private fun map(list: List<Pair<Rating, Movie>>): List<GetTopMoviesResponse> =
        list.map {
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
