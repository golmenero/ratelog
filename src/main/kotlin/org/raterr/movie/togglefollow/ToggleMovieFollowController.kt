package org.raterr.movie.togglefollow

import org.raterr.annotations.CurrentUser
import org.raterr.movie.Movie
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import jakarta.servlet.http.HttpServletRequest

@Controller
class ToggleMovieFollowController(
    private val handler: ToggleMovieFollowHandler,
) {

    @PostMapping("/movie/follow")
    fun toggleMovieFollow(
        @CurrentUser user: User,
        @RequestParam("movieId") movieId: Long,
        request: HttpServletRequest
    ): String {
        ToggleMovieFollow(
            movieId = Movie.Id(movieId),
            userId = user.id!!,
        ).let(handler::handle)

        val referer = request.getHeader("Referer")
        return "redirect:$referer"
    }
}
