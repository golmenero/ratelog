package org.raterr.tvshow.rating.addseason

import org.raterr.TmdbId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AddSeasonRatingController(
    private val handler: AddSeasonRatingHandler,
) {

    @PostMapping("/tv/rate")
    fun saveRating(
        @CurrentUser user: User,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("seasonNumber") seasonNumber: Int,
        @RequestParam("directing") directing: Double,
        @RequestParam("cinematography") cinematography: Double,
        @RequestParam("acting") acting: Double,
        @RequestParam("soundtrack") soundtrack: Double,
        @RequestParam("screenplay") screenplay: Double,
        redirectAttributes: RedirectAttributes
    ): String =
        AddSeasonRating(
            tmdbId = TmdbId(tmdbId),
            seasonNumber = seasonNumber,
            userId = user.id!!,
            directing = directing,
            cinematography = cinematography,
            acting = acting,
            soundtrack = soundtrack,
            screenplay = screenplay,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addAttribute("id", tmdbId)
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/tv/rate"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "Rating saved successfully.")
                    "redirect:/tvshows"
                }
            )

    private fun mapError(error: AddSeasonRatingHandlerError): String = when (error) {
        AddSeasonRatingHandlerError.InvalidRatingValue -> "Invalid rating value."
        AddSeasonRatingHandlerError.RatingAlreadyExists -> "A rating already exists for this season."
        AddSeasonRatingHandlerError.TvShowNotFound -> "Could not load the TV show."
    }
}
