package org.ratelog.movie.rating.add

import org.ratelog.annotations.CurrentUser
import org.ratelog.movie.Movie
import org.ratelog.user.User
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Locale

@Controller
class AddRatingController(
    private val handler: AddRatingHandler,
    private val messageSource: MessageSource,
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
        redirectAttributes: RedirectAttributes,
        locale: Locale
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
                    redirectAttributes.addFlashAttribute("success", messageSource.getMessage("success.rating.saved", null, locale))
                    "redirect:/movie/${tmdbId}"
                }
            )

    private fun mapError(error: AddRatingHandlerError): String = when (error) {
        AddRatingHandlerError.InvalidRatingValue -> "Invalid rating value."
        AddRatingHandlerError.RatingAlreadyExists -> "A rating already exists for this movie."
        AddRatingHandlerError.MovieNotFound -> "Could not load the movie."
    }
}
