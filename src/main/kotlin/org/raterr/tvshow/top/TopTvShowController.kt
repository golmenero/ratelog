package org.raterr.tvshow.top

import org.raterr.UserId
import org.raterr.tvrating.TvRatingScoreService
import org.raterr.tvrating.TvRating
import org.raterr.annotations.CurrentUser
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class GetTopTvShowsResponse(
    val tmdbId: Int,
    val name: String,
    val firstAirYear: Int?,
    val posterPath: String?,
    val averageScore: Double,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double
)

@Controller
class TopTvShowController(
    private val handler: TopTvShowHandler,
) {

    @GetMapping("/tv/top")
    fun topsPage(
        @CurrentUser user: User,
        @RequestParam("year", required = false) year: Int?,
        @RequestParam("category", required = false) category: String?,
        model: Model
    ): String {
        val tops = TopTvShow(
            userId = user.id!!.let(::UserId),
            year = year,
            category = category
        ).let(handler::handle)

        model.addAttribute("tops", tops.let(::map))
        model.addAttribute("selectedYear", year)
        model.addAttribute("selectedCategory", category)

        return "tv-top"
    }

    private fun map(list: List<Pair<TvRating, TvShow>>): List<GetTopTvShowsResponse> =
        list.map {
            GetTopTvShowsResponse(
                tmdbId = it.second.tmdbId,
                name = it.second.name,
                firstAirYear = it.second.firstAirYear,
                posterPath = it.second.posterPath,
                averageScore = TvRatingScoreService.score(it.first),
                directing = it.first.directing,
                cinematography = it.first.cinematography,
                acting = it.first.acting,
                soundtrack = it.first.soundtrack,
                screenplay = it.first.screenplay
            )
        }
}
