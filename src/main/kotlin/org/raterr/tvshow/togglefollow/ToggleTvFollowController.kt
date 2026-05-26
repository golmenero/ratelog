package org.raterr.tvshow.togglefollow

import org.raterr.annotations.CurrentUser
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import jakarta.servlet.http.HttpServletRequest

@Controller
class ToggleTvFollowController(
    private val handler: ToggleTvFollowHandler,
) {

    @PostMapping("/tvshow/follow")
    fun toggleTvFollow(
        @CurrentUser user: User,
        @RequestParam("tvShowId") tvShowId: Long,
        request: HttpServletRequest
    ): String {
        ToggleTvFollow(
            tvShowId = TvShow.Id(tvShowId),
            userId = user.id!!,
        ).let(handler::handle)

        val referer = request.getHeader("Referer")
        return "redirect:$referer"
    }
}
