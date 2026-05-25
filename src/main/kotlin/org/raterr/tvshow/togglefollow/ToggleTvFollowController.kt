package org.raterr.tvshow.togglefollow

import org.raterr.TmdbId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ToggleTvFollowController(
    private val handler: ToggleTvFollowHandler,
) {

    @PostMapping("/tvshow/follow")
    fun toggleTvFollow(
        @CurrentUser user: User,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("q", required = false) query: String?
    ): String {
        ToggleTvFollow(
            tmdbId = TmdbId(tmdbId),
            userId = user.id!!,
        ).let(handler::handle)

        return if (!query.isNullOrBlank()) "redirect:/?q=${query}" else "redirect:/"
    }
}
