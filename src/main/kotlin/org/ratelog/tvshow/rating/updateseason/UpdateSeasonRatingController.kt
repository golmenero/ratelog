package org.ratelog.tvshow.rating.updateseason

import org.ratelog.SeasonNumber
import org.ratelog.annotations.CurrentUser
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class UpdateSeasonRatingController(
    private val handler: UpdateSeasonRatingHandler,
) {

    @PostMapping("/tv/rate/update")
    fun updateRating(
        @CurrentUser user: User,
        @RequestParam("tvShowId") tvShowId: Long,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("seasonNumber") seasonNumber: Int,
        @RequestParam("directing") directing: Double,
        @RequestParam("cinematography") cinematography: Double,
        @RequestParam("acting") acting: Double,
        @RequestParam("soundtrack") soundtrack: Double,
        @RequestParam("screenplay") screenplay: Double,
        @RequestParam("review", required = false) review: String?,
        redirectAttributes: RedirectAttributes
    ): String =
        UpdateSeasonRating(
            tvShowId = TvShow.Id(tvShowId),
            seasonNumber = seasonNumber.let(::SeasonNumber),
            userId = user.id!!,
            directing = directing,
            cinematography = cinematography,
            acting = acting,
            soundtrack = soundtrack,
            screenplay = screenplay,
            review = review?.takeIf { it.isNotBlank() },
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/tv/${tmdbId}"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "success.rating.updated")
                    "redirect:/tv/${tmdbId}"
                }
            )

    private fun mapError(error: UpdateSeasonRatingHandlerError): String = when (error) {
        UpdateSeasonRatingHandlerError.InvalidRatingValue -> "error.rating.invalid"
        UpdateSeasonRatingHandlerError.RatingNotFound,
        UpdateSeasonRatingHandlerError.SeasonRatingNotFound -> "error.rating.update.failed"
    }
}
