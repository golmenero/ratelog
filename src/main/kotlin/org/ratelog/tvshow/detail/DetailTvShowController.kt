package org.ratelog.tvshow.detail

import org.ratelog.TmdbId
import org.ratelog.annotations.CurrentUser
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
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
    val genres: List<String>,
    val seasons: List<SeasonResponse>,
    val overallScore: Double?,
    val isFollowed: Boolean,
    val hasRating: Boolean,
)

data class SeasonResponse(
    val seasonNumber: Int,
    val hasRating: Boolean,
    val directing: Double?,
    val cinematography: Double?,
    val acting: Double?,
    val soundtrack: Double?,
    val screenplay: Double?,
    val score: Double?,
    val review: String?,
)

@Controller
class DetailTvShowController(
    private val handler: DetailTvShowHandler,
) {

    @GetMapping("/tv/{id}")
    fun detailPage(
        @CurrentUser user: User,
        @PathVariable("id") tmdbId: Int,
        model: Model
    ): String =
        GetTvShowDetail(
            userId = user.id!!,
            tmdbId = TmdbId(tmdbId),
            lang = user.lang,
            )
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "error.load.tvshow")
                    "tvshow-detail"
                },
                {
                    model.addAttribute("show", buildResponse(it))
                    "tvshow-detail"
                }
            )

    private fun buildResponse(result: GetTvShowDetailResult): TvShowDetailResponse =
        TvShowDetailResponse(
            id = result.id,
            tmdbId = result.tmdbId,
            name = result.title,
            overview = result.overview,
            firstAirYear = result.firstAirYear,
            posterPath = result.posterPath,
            tmdbVoteAverage = result.tmdbVoteAverage,
            genres = result.genres,
            seasons = result.seasons.map { s ->
                SeasonResponse(
                    seasonNumber = s.seasonNumber,
                    hasRating = s.rating != null,
                    directing = s.rating?.directing,
                    cinematography = s.rating?.cinematography,
                    acting = s.rating?.acting,
                    soundtrack = s.rating?.soundtrack,
                    screenplay = s.rating?.screenplay,
                    score = s.rating?.score,
                    review = s.rating?.review,
                )
            },
            overallScore = result.overallScore,
            isFollowed = result.isFollowed,
            hasRating = result.isRated,
        )
}
