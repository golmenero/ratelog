package org.ratelog.tvshow.togglefollow

import org.ratelog.annotations.CurrentUser
import org.ratelog.tvshow.TvShow
import org.ratelog.user.User
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
