package org.raterr.premieres

import org.raterr.TmdbClient
import org.raterr.annotations.CurrentUser
import org.raterr.follow.FollowRepository
import org.raterr.user.User
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

data class PremiereItem(
    val tmdbId: Int,
    val title: String,
    val releaseDate: LocalDate,
    val type: String,
    val posterPath: String?,
    val isReleased: Boolean,
    val hasDate: Boolean = true
)

@Controller
class PremieresController(
    private val tmdbClient: TmdbClient,
    private val followRepository: FollowRepository,
) {

    @GetMapping("/premieres")
    fun premieresPage(
        @CurrentUser user: User,
        model: Model
    ): String {
        val (released, upcoming, noDate) = getFollowedPremieres(user.id!!)
        model.addAttribute("releasedPremieres", released)
        model.addAttribute("upcomingPremieres", upcoming)
        model.addAttribute("noDatePremieres", noDate)
        return "premieres"
    }

    private fun getFollowedPremieres(userId: Long): Triple<List<PremiereItem>, List<PremiereItem>, List<PremiereItem>> {
        val follows = followRepository.findByUserId(userId)
        val released = mutableListOf<PremiereItem>()
        val upcoming = mutableListOf<PremiereItem>()
        val noDate = mutableListOf<PremiereItem>()
        val today = LocalDate.now()

        for (follow in follows) {
            val (releaseDateStr, title, posterPath) = when (follow.contentType) {
                "movie" -> {
                    val movie = tmdbClient.movieDetails(follow.contentTmdbId)
                    Triple(movie.releaseDate, movie.title, movie.posterPath)
                }
                "tvshow" -> {
                    val show = tmdbClient.tvShowDetails(follow.contentTmdbId)
                    Triple(show.firstAirDate, show.name, show.posterPath)
                }
                else -> Triple(null, "Unknown", null)
            }

            if (!releaseDateStr.isNullOrBlank()) {
                val date = LocalDate.parse(releaseDateStr)
                val item = PremiereItem(
                    tmdbId = follow.contentTmdbId,
                    title = title,
                    releaseDate = date,
                    type = follow.contentType,
                    posterPath = posterPath,
                    isReleased = date <= today
                )
                if (item.isReleased) released.add(item) else upcoming.add(item)
            } else {
                noDate.add(
                    PremiereItem(
                        tmdbId = follow.contentTmdbId,
                        title = title,
                        releaseDate = today,
                        type = follow.contentType,
                        posterPath = posterPath,
                        isReleased = false,
                        hasDate = false
                    )
                )
            }
        }

        return Triple(
            released.sortedBy { it.releaseDate },
            upcoming.sortedBy { it.releaseDate },
            noDate
        )
    }
}
