package org.raterr.tvshow.follow.toggle

import org.raterr.annotations.CurrentUser
import org.raterr.tvshow.TvShow
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
        @RequestParam("tvShowId") tvShowId: Long,
        @RequestParam("q", required = false) query: String?
    ): String {
        ToggleTvFollow(
            tvShowId = TvShow.Id(tvShowId),
            userId = user.id!!,
        ).let(handler::handle)

        return if (!query.isNullOrBlank()) "redirect:/?q=${query}" else "redirect:/"
    }
}
