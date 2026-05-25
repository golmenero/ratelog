package org.raterr.tvshow.top

import org.raterr.tvrating.TvRatingScoreService
import org.raterr.annotations.CurrentUser
import org.raterr.tvshow.premieres.TvShowPremieresHandler
import org.raterr.tvshow.premieres.TvShowPremieresQuery
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class GetTopTvShowsResponse(
    val rank: Int,
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
    private val tvShowPremieresHandler: TvShowPremieresHandler,
) {

    @GetMapping("/tvshows")
    fun topsPage(
        @CurrentUser user: User,
        @RequestParam("category", required = false) category: String?,
        @RequestParam("limit", required = false, defaultValue = "10") limit: Int,
        @RequestParam("name", required = false) name: String?,
        model: Model
    ): String {
        val tops = TopTvShow(
            userId = user.id!!,
            category = category,
            limit = limit,
            name = name
        ).let(handler::handle)

        model.addAttribute("tops", tops.let(::map))
        model.addAttribute("selectedCategory", category)
        model.addAttribute("selectedLimit", limit)
        model.addAttribute("selectedName", name)

        TvShowPremieresQuery(user.id).let(tvShowPremieresHandler::handle)
            .fold(
                { },
                {
                    model.addAttribute("releasedPremieres", it.released)
                    model.addAttribute("upcomingPremieres", it.upcoming)
                    model.addAttribute("noDatePremieres", it.noDate)
                }
            )

        return "tvshows"
    }

    private fun map(list: List<RankedTvShow>): List<GetTopTvShowsResponse> =
        list.map {
            GetTopTvShowsResponse(
                rank = it.rating.rank.value,
                tmdbId = it.show.tmdbId.value,
                name = it.show.name.value,
                firstAirYear = it.show.firstAirYear,
                posterPath = it.show.posterPath?.value,
                averageScore = TvRatingScoreService.score(it.rating),
                directing = it.rating.directing.value,
                cinematography = it.rating.cinematography.value,
                acting = it.rating.acting.value,
                soundtrack = it.rating.soundtrack.value,
                screenplay = it.rating.screenplay.value,
            )
        }
}
