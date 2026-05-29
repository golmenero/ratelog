package org.ratelog.tvshow.rating.deleteseason

import org.ratelog.SeasonNumber
import org.ratelog.annotations.CurrentUser
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import jakarta.servlet.http.HttpServletRequest

@Controller
class DeleteSeasonRatingController(
    private val handler: DeleteSeasonRatingHandler,
) {

    @PostMapping("/tvshows/delete/{id}")
    @Transactional
    fun deleteRating(
        @CurrentUser user: User,
        @PathVariable("id") tvShowId: Long,
        @RequestParam("seasonNumber") seasonNumber: Int,
        redirectAttributes: RedirectAttributes,
        request: HttpServletRequest
    ): String {
        val result = DeleteSeasonRating(
            tvShowId = TvShow.Id(tvShowId),
            seasonNumber = seasonNumber.let(::SeasonNumber),
            userId = user.id!!,
        ).let(handler::handle)

        val referer = request.getHeader("Referer")
        val redirectTarget = when {
            !referer.isNullOrBlank() && referer.contains("/tv/") -> "redirect:$referer"
            else -> "redirect:/tvshows"
        }

        return result
            .mapLeft(::mapError)
            .fold(
                {
                    redirectAttributes.addFlashAttribute("error", it)
                    redirectTarget
                },
                {
                    redirectAttributes.addFlashAttribute("success", "Rating deleted successfully.")
                    redirectTarget
                }
            )
    }

    private fun mapError(error: DeleteSeasonRatingHandlerError): String = when (error) {
        DeleteSeasonRatingHandlerError.TvShowNotFound,
        DeleteSeasonRatingHandlerError.RatingNotFound -> "Could not delete the rating."
    }
}
