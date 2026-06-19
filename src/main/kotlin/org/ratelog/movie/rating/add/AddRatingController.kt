package org.ratelog.movie.rating.add

import org.ratelog.annotations.CurrentUser
import org.ratelog.movie.Movie
import org.ratelog.user.User
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
        @RequestParam("review", required = false) review: String?,
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
            review = review?.takeIf { it.isNotBlank() },
        ).let(handler::handle)
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    "redirect:/movie/${tmdbId}"
                },
                {
                    redirectAttributes.addFlashAttribute("success", "success.rating.saved")
                    "redirect:/movie/${tmdbId}"
                }
            )

    private fun mapError(error: AddRatingHandlerError): String = when (error) {
        AddRatingHandlerError.InvalidRatingValue -> "error.rating.invalid"
        AddRatingHandlerError.RatingAlreadyExists -> "error.rating.exists.movie"
        AddRatingHandlerError.MovieNotFound -> "error.load.movie"
    }
}
