package org.raterr.follow.toggle

import org.raterr.MediaType
import org.raterr.TmdbId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ToggleFollowController(
    private val handler: ToggleFollowHandler,
) {

    @PostMapping("/follow")
    fun toggleFollow(
        @CurrentUser user: User,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("type") type: String,
        @RequestParam("q", required = false) query: String?
    ): String {
        ToggleFollow(
            tmdbId = tmdbId.let(::TmdbId),
            userId = user.id!!,
            type = type.let(MediaType::valueOf),
        ).let(handler::handle)

        return if (!query.isNullOrBlank()) "redirect:/?q=${query}" else "redirect:/"
    }
}