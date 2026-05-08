package org.raterr.tvrating

import org.raterr.tvshow.TvShowRepository
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.NoSuchElementException

@Controller
class DeleteTvRatingController(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val userService: UserService
) {

    @PostMapping("/tv/top/delete/{id}")
    @Transactional
    fun deleteRating(
        @PathVariable("id") tmdbId: Int,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            val user = userService.getRequiredCurrentUser()

            val show = tvShowRepository.findByTmdbId(tmdbId)
                .orElseThrow { NoSuchElementException("TV show not found") }

            val deletedCount = tvRatingRepository.deleteByTvShowIdAndUserId(show.id!!, user.id!!)

            if (deletedCount == 0) {
                throw NoSuchElementException("Rating not found")
            }

            redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
            "redirect:/tv/top"
        } catch (e: NoSuchElementException) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating.")
            "redirect:/tv/top"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Could not delete the rating.")
            "redirect:/tv/top"
        }
    }
}
