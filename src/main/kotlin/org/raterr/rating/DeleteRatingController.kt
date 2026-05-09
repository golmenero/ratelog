package org.raterr.rating

import org.raterr.movie.MovieRepository
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.NoSuchElementException

@Controller
class DeleteRatingController(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val userService: UserService
) {

    @PostMapping("/movie/top/delete/{id}")
    @Transactional
    fun deleteRating(
        @PathVariable("id") tmdbId: Int,
        redirectAttributes: RedirectAttributes
    ): String {
        val userId = userService.getCurrentUserId()!!

        return try {

            val movie = movieRepository.findByTmdbId(tmdbId)
                .orElseThrow { NoSuchElementException("Movie not found") }

            val deletedCount = ratingRepository.deleteByMovieIdAndUserId(movie.id!!, userId)

            if (deletedCount == 0) {
                throw NoSuchElementException("Rating not found")
            }

            redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
            "redirect:/movie/top"
        } catch (e: NoSuchElementException) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating.")
            "redirect:/movie/top"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating.")
            "redirect:/movie/top"
        }
    }
}
