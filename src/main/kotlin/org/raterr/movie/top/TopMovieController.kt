package org.raterr.movie.top

import org.raterr.UserId
import org.raterr.rating.RatingScoreService
import org.raterr.rating.Rating
import org.raterr.annotations.CurrentUser
import org.raterr.movie.Movie
import org.raterr.movie.premieres.MoviePremieresHandler
import org.raterr.movie.premieres.MoviePremieresQuery
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

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
    private val moviePremieresHandler: MoviePremieresHandler,
) {

    @GetMapping("/movies")
    fun topsPage(
        @CurrentUser user: User,
        @RequestParam("category", required = false) category: String?,
        @RequestParam("limit", required = false, defaultValue = "10") limit: Int,
        @RequestParam("name", required = false) name: String?,
        model: Model
    ): String {
        val tops = TopMovie(
            userId = user.id!!.let(::UserId),
            category = category,
            limit = limit,
            name = name
        ).let(handler::handle)

        model.addAttribute("tops", tops.let(::map))
        model.addAttribute("selectedCategory", category)
        model.addAttribute("selectedLimit", limit)
        model.addAttribute("selectedName", name)

        MoviePremieresQuery(UserId(user.id!!)).let(moviePremieresHandler::handle)
            .fold(
                { },
                {
                    model.addAttribute("releasedPremieres", it.released)
                    model.addAttribute("upcomingPremieres", it.upcoming)
                    model.addAttribute("noDatePremieres", it.noDate)
                }
            )

        return "movies"
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
