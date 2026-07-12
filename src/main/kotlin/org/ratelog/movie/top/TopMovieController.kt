package org.ratelog.movie.top

import org.ratelog.annotations.CurrentUser
import org.ratelog.movie.premieres.MoviePremieresHandler
import org.ratelog.movie.premieres.MoviePremieresQuery
import org.ratelog.user.User
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
    val genreIds: List<Int>
)

@Controller
class TopMovieController(
    private val handler: TopMovieHandler,
    private val moviePremieresHandler: MoviePremieresHandler,
) {

    @GetMapping("/movies")
    fun topsPage(
        @CurrentUser user: User,
        @RequestParam("category", required = false) genreId: String?,
        @RequestParam("limit", required = false, defaultValue = "10") limit: Int,
        @RequestParam("name", required = false) name: String?,
        model: Model
    ): String {
        val tops = TopMovie(
            userId = user.id!!,
            genreId = genreId,
            limit = limit,
            name = name,
            lang = user.metadataLang,
        ).let(handler::handle)

        model.addAttribute("tops", tops.map { toResponse(it) })
        model.addAttribute("selectedCategory", genreId)
        model.addAttribute("selectedLimit", limit)
        model.addAttribute("selectedName", name)

        MoviePremieresQuery(user.id, user.metadataLang).let(moviePremieresHandler::handle)
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

    private fun toResponse(item: TopMovieItem) = GetTopMoviesResponse(
        rank = item.rank.value,
        tmdbId = item.movie.tmdbId.value,
        title = item.title,
        releaseYear = item.movie.releaseYear,
        posterPath = item.movie.posterPath?.value,
        averageScore = item.rating.score?.value ?: 0.0,
        genreIds = item.movie.genres.map { it.tmdbId }
    )
}
