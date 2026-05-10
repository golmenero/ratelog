package org.raterr.rating.delete

import org.raterr.TmdbId
import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class DeleteRatingController(
    private val handler: DeleteRatingHandler,
) {

    @PostMapping("/movie/top/delete/{id}")
    @Transactional
    fun deleteRating(
        @CurrentUser user: User,
        @PathVariable("id") tmdbId: Int,
        redirectAttributes: RedirectAttributes
    ): String =
        DeleteRating(
            tmdbId = TmdbId(tmdbId),
            userId = UserId(user.id!!),
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/movie/top"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
                    "redirect:/movie/top"
                }
            )

    private fun mapError(error: DeleteRatingHandlerError): String = when (error) {
        DeleteRatingHandlerError.MovieNotFound,
        DeleteRatingHandlerError.RatingNotFound -> "Could not delete the rating."
    }
}
