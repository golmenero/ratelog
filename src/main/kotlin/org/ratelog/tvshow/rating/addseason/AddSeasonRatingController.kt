package org.ratelog.tvshow.rating.addseason

import org.ratelog.Score
import org.ratelog.SeasonNumber
import org.ratelog.annotations.CurrentUser
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Locale

@Controller
class AddSeasonRatingController(
    private val handler: AddSeasonRatingHandler,
    private val messageSource: MessageSource,
) {

    @PostMapping("/tv/rate")
    fun saveRating(
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
        redirectAttributes: RedirectAttributes,
        locale: Locale
    ): String =
        AddSeasonRating(
            tvShowId = TvShow.Id(tvShowId),
            seasonNumber = seasonNumber.let(::SeasonNumber),
            userId = user.id!!,
            directing = directing.let(::Score),
            cinematography = cinematography.let(::Score),
            acting = acting.let(::Score),
            soundtrack = soundtrack.let(::Score),
            screenplay = screenplay.let(::Score),
            review = review?.takeIf { it.isNotBlank() },
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/tv/${tmdbId}"
                },
                {
                    redirectAttributes.addFlashAttribute("success", messageSource.getMessage("success.rating.saved", null, locale))
                    "redirect:/tv/${tmdbId}"
                }
            )

    private fun mapError(error: AddSeasonRatingHandlerError): String = when (error) {
        AddSeasonRatingHandlerError.InvalidRatingValue -> "Invalid rating value."
        AddSeasonRatingHandlerError.RatingAlreadyExists -> "A rating already exists for this season."
        AddSeasonRatingHandlerError.TvShowNotFound -> "Could not load the TV show."
    }
}
