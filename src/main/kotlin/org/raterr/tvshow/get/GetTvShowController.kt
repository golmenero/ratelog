package org.raterr.tvshow.get

import org.raterr.TmdbId
import org.raterr.tvshow.TvShow
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class GetTvShowDetailsResponse(
    val tmdbId: Int,
    val name: String,
    val overview: String?,
    val firstAirDate: String?,
    val firstAirYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
)

@Controller
class GetTvShowController(
    private val handler: GetTvShowHandler,
) {

    @GetMapping("/tv/rate")
    fun ratePage(@RequestParam("id") tmdbId: Int, model: Model): String =
        GetTvShow(tmdbId = TmdbId(tmdbId))
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load the TV show.")
                    "tv-rate"
                },
                {
                    model.addAttribute("show", buildResponse(it))
                    "tv-rate"
                }
            )

    private fun buildResponse(show: TvShow): GetTvShowDetailsResponse =
        GetTvShowDetailsResponse(
            tmdbId = show.tmdbId.value,
            name = show.name.value,
            overview = show.overview?.value,
            firstAirDate = show.firstAirDate.toString(),
            firstAirYear = show.firstAirYear,
            posterPath = show.posterPath?.value,
            tmdbVoteAverage = show.tmdbVoteAverage,
        )

}
