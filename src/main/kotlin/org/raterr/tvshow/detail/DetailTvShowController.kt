package org.raterr.tvshow.detail

import org.raterr.TmdbId
import org.raterr.tvshow.rating.TvRatingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

data class TvShowDetailResponse(
    val id: Long,
    val tmdbId: Int,
    val name: String,
    val overview: String?,
    val firstAirYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val seasonRatings: List<SeasonRatingResponse>,
    val overallScore: Double?,
    val isFollowed: Boolean,
    val hasRating: Boolean,
)

data class SeasonRatingResponse(
    val seasonNumber: Int,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val score: Double,
)

@Controller
class DetailTvShowController(
    private val handler: DetailTvShowHandler,
    private val tvRatingRepository: TvRatingRepository,
) {

    @GetMapping("/tv/{id}")
    fun detailPage(
        @PathVariable("id") tmdbId: Int,
        model: Model
    ): String =
        GetTvShowDetail(tmdbId = TmdbId(tmdbId))
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load the TV show.")
                    "tvshow-detail"
                },
                {
                    model.addAttribute("show", buildResponse(it))
                    "tvshow-detail"
                }
            )

    private fun buildResponse(result: GetTvShowDetailResult): TvShowDetailResponse =
        TvShowDetailResponse(
            id = result.show.id!!.value,
            tmdbId = result.show.tmdbId.value,
            name = result.show.name.value,
            overview = result.show.overview?.value,
            firstAirYear = result.show.firstAirYear,
            posterPath = result.show.posterPath?.value,
            tmdbVoteAverage = result.show.tmdbVoteAverage,
            seasonRatings = result.seasonRatings.map { sr ->
                SeasonRatingResponse(
                    seasonNumber = sr.seasonNumber,
                    directing = sr.directing,
                    cinematography = sr.cinematography,
                    acting = sr.acting,
                    soundtrack = sr.soundtrack,
                    screenplay = sr.screenplay,
                    score = sr.score,
                )
            },
            overallScore = result.overallScore,
            isFollowed = result.show.followed,
            hasRating = result.show.id.let { tvRatingRepository.findFirstByTvShowId(it) } != null,
        )
}
