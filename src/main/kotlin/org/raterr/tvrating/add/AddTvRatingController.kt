package org.raterr.tvrating.add

import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AddTvRatingController(
    private val handler: AddTvRatingHandler,
) {

    @PostMapping("/tv/rate")
    fun saveRating(
        @CurrentUser user: User,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("directing") directing: Double,
        @RequestParam("cinematography") cinematography: Double,
        @RequestParam("acting") acting: Double,
        @RequestParam("soundtrack") soundtrack: Double,
        @RequestParam("screenplay") screenplay: Double,
        redirectAttributes: RedirectAttributes
    ): String =
        AddTvRating(
            tmdbId = TmdbId(tmdbId),
            userId = UserId(user.id!!),
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
                    "redirect:/tv/top"
                }
            )

    private fun mapError(error: AddTvRatingHandlerError): String = when (error) {
        AddTvRatingHandlerError.InvalidRatingValue -> "Invalid rating value."
        AddTvRatingHandlerError.RatingAlreadyExists -> "A rating already exists for this TV show."
        AddTvRatingHandlerError.TvShowNotFound -> "Could not load the TV show."
    }
}
