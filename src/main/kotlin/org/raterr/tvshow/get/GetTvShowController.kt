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
    val seasons: List<SeasonResponse>
)

data class SeasonResponse(
    val seasonNumber: Int,
    val episodeCount: Int?,
    val airDate: String?
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

    private fun buildResponse(result: GetTvShowResult): GetTvShowDetailsResponse =
        GetTvShowDetailsResponse(
            tmdbId = result.show.tmdbId.value,
            name = result.show.name.value,
            overview = result.show.overview?.value,
            firstAirDate = result.show.firstAirDate.toString(),
            firstAirYear = result.show.firstAirYear,
            posterPath = result.show.posterPath?.value,
            tmdbVoteAverage = result.show.tmdbVoteAverage,
            seasons = result.seasons.map { s ->
                SeasonResponse(
                    seasonNumber = s.seasonNumber,
                    episodeCount = s.episodeCount,
                    airDate = s.airDate
                )
            }
        )

}
