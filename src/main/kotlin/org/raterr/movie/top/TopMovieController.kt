package org.raterr.movie.top

import org.raterr.annotations.CurrentUser
import org.raterr.movie.premieres.MoviePremieresHandler
import org.raterr.movie.premieres.MoviePremieresQuery
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class GetTopMoviesResponse(
    val rank: Int,
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
            userId = user.id!!,
            category = category,
            limit = limit,
            name = name
        ).let(handler::handle)

        model.addAttribute("tops", tops.let(::map))
        model.addAttribute("selectedCategory", category)
        model.addAttribute("selectedLimit", limit)
        model.addAttribute("selectedName", name)

        MoviePremieresQuery(user.id).let(moviePremieresHandler::handle)
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

    private fun map(list: List<TopMovieItem>): List<GetTopMoviesResponse> =
        list.mapIndexed { index, item ->
            GetTopMoviesResponse(
                rank = index + 1,
                tmdbId = item.movie.tmdbId.value,
                title = item.movie.title.value,
                releaseYear = item.movie.releaseYear,
                posterPath = item.movie.posterPath?.value,
                averageScore = item.rating.score.value,
                directing = item.rating.directing.value,
                cinematography = item.rating.cinematography.value,
                acting = item.rating.acting.value,
                soundtrack = item.rating.soundtrack.value,
                screenplay = item.rating.screenplay.value,
            )
        }
}
