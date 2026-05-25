package org.raterr.tvrating.delete

import org.raterr.TmdbId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class DeleteTvRatingController(
    private val handler: DeleteTvRatingHandler,
) {

    @PostMapping("/tvshows/delete/{id}")
    @Transactional
    fun deleteRating(
        @CurrentUser user: User,
        @PathVariable("id") tmdbId: Int,
        redirectAttributes: RedirectAttributes
    ): String =
        DeleteTvRating(
            tmdbId = TmdbId(tmdbId),
            userId = user.id!!,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/tvshows"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
                    "redirect:/tvshows"
                }
            )

    private fun mapError(error: DeleteTvRatingHandlerError): String = when (error) {
        DeleteTvRatingHandlerError.TvShowNotFound,
        DeleteTvRatingHandlerError.RatingNotFound -> "Could not delete the rating."
    }
}
