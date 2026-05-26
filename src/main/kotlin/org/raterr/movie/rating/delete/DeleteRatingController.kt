package org.raterr.movie.rating.delete

import org.raterr.TmdbId
import org.raterr.annotations.CurrentUser
import org.raterr.movie.Movie
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

    @PostMapping("/movies/delete/{id}")
    @Transactional
    fun deleteRating(
        @CurrentUser user: User,
        @PathVariable("id") movieId: Long,
        redirectAttributes: RedirectAttributes
    ): String =
        DeleteRating(
            movieId = Movie.Id(movieId),
            userId = user.id!!,
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/movies"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
                    "redirect:/movies"
                }
            )

    private fun mapError(error: DeleteRatingHandlerError): String = when (error) {
        DeleteRatingHandlerError.MovieNotFound,
        DeleteRatingHandlerError.RatingNotFound -> "Could not delete the rating."
    }
}
