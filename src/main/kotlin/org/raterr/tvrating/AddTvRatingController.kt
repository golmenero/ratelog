package org.raterr.tvrating

import org.raterr.TmdbClient
import org.raterr.TmdbTvShow
import org.raterr.follow.FollowRepository
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AddTvRatingController(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService
) {

    @PostMapping("/tv/rate")
    fun saveRating(
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("directing") directing: Double,
        @RequestParam("cinematography") cinematography: Double,
        @RequestParam("acting") acting: Double,
        @RequestParam("soundtrack") soundtrack: Double,
        @RequestParam("screenplay") screenplay: Double,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            listOf(
                "directing" to directing,
                "cinematography" to cinematography,
                "acting" to acting,
                "soundtrack" to soundtrack,
                "screenplay" to screenplay
            ).forEach { (field, value) ->
                require(value >= 1.0 && value <= 10.0) { "Field $field must be between 1 and 10" }
            }

            val user = userService.getRequiredCurrentUser()

            val show = tvShowRepository.findByTmdbId(tmdbId).orElse(null)
                ?: upsertTvShow(tmdbClient.tvShowDetails(tmdbId))

            val existingRating = tvRatingRepository.findByTvShowIdAndUserId(show.id!!, user.id!!).firstOrNull()
            if (existingRating != null) {
                redirectAttributes.addAttribute("id", tmdbId)
                redirectAttributes.addFlashAttribute("error", "A rating already exists for this TV show.")
                return "redirect:/tv/rate"
            }

            val newRating = TvRating(
                tvShowId = show.id!!,
                userId = user.id!!,
                directing = directing,
                cinematography = cinematography,
                acting = acting,
                soundtrack = soundtrack,
                screenplay = screenplay,
                createdAtEpochMs = System.currentTimeMillis()
            )
            tvRatingRepository.save(newRating)

            followRepository.findByUserIdAndContentTypeAndContentTmdbId(user.id!!, "tvshow", tmdbId).ifPresent { follow ->
                followRepository.delete(follow)
            }

            redirectAttributes.addFlashAttribute("success", "Rating saved successfully.")
            "redirect:/tv/top"
        } catch (e: IllegalArgumentException) {
            redirectAttributes.addAttribute("id", tmdbId)
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/tv/rate"
        } catch (e: Exception) {
            redirectAttributes.addAttribute("id", tmdbId)
            redirectAttributes.addFlashAttribute("error", "Could not save the rating.")
            "redirect:/tv/rate"
        }
    }

    private fun upsertTvShow(tmdbShow: TmdbTvShow): TvShow {
        val existing = tvShowRepository.findByTmdbId(tmdbShow.id).orElse(null)

        return if (existing != null) {
            val updated = existing.copy(
                name = tmdbShow.name,
                originalName = tmdbShow.originalName,
                overview = tmdbShow.overview,
                firstAirDate = tmdbShow.firstAirDate,
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = tmdbShow.genres.joinToString(",") { it.name }
            )
            tvShowRepository.save(updated)
        } else {
            val newShow = TvShow(
                tmdbId = tmdbShow.id,
                name = tmdbShow.name,
                originalName = tmdbShow.originalName,
                overview = tmdbShow.overview,
                firstAirDate = tmdbShow.firstAirDate,
                firstAirYear = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                genres = tmdbShow.genres.joinToString(",") { it.name }
            )
            tvShowRepository.save(newShow)
        }
    }
}
