package org.raterr.tvshow.top

import org.raterr.annotations.CurrentUser
import org.raterr.tvshow.premieres.TvShowPremieresHandler
import org.raterr.tvshow.premieres.TvShowPremieresQuery
import org.raterr.tvshow.rating.TvRating
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.collections.map

data class GetTopTvShowsResponse(
    val rank: Int,
    val tmdbId: Int,
    val name: String,
    val firstAirYear: Int?,
    val posterPath: String?,
    val averageScore: Double,
    val seasons: List<GetSeasonResponse>
)

data class GetSeasonResponse(
    val seasonNumber: Int,
    val score: Double,
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

    private fun map(list: List<TopTvShowItem>): List<GetTopTvShowsResponse> =
        list.mapIndexed { index, item ->
            GetTopTvShowsResponse(
                rank = index + 1,
                tmdbId = item.tvShow.tmdbId.value,
                name = item.tvShow.name.value,
                firstAirYear = item.tvShow.firstAirYear,
                posterPath = item.tvShow.posterPath?.value,
                averageScore = item.rating.score?.value ?: 0.0,
                seasons = item.rating.seasonRatings.map { sr ->
                    GetSeasonResponse(
                        seasonNumber = sr.seasonNumber.value,
                        score = sr.score.value,
                        directing = sr.directing.value,
                        cinematography = sr.cinematography.value,
                        acting = sr.acting.value,
                        soundtrack = sr.soundtrack.value,
                        screenplay = sr.screenplay.value
                    )
                }
            )
        }
}
