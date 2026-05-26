package org.raterr.movie.rating.add

import org.raterr.annotations.CurrentUser
import org.raterr.movie.Movie
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AddRatingController(
    private val handler: AddRatingHandler,
) {

    @PostMapping("/movie/rate")
    fun saveRating(
        @CurrentUser user: User,
        @RequestParam("movieId") movieId: Long,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("directing") directing: Double,
        @RequestParam("cinematography") cinematography: Double,
        @RequestParam("acting") acting: Double,
        @RequestParam("soundtrack") soundtrack: Double,
        @RequestParam("screenplay") screenplay: Double,
        redirectAttributes: RedirectAttributes
    ): String =
        AddRating(
            movieId = Movie.Id(movieId),
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
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/movie/${tmdbId}"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "Rating saved successfully.")
                    "redirect:/movie/${tmdbId}"
                }
            )

    private fun mapError(error: AddRatingHandlerError): String = when (error) {
        AddRatingHandlerError.InvalidRatingValue -> "Invalid rating value."
        AddRatingHandlerError.RatingAlreadyExists -> "A rating already exists for this movie."
        AddRatingHandlerError.MovieNotFound -> "Could not load the movie."
    }
}
