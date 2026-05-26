package org.raterr.movie.togglefollow

import org.raterr.TmdbId
import org.raterr.annotations.CurrentUser
import org.raterr.movie.Movie
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ToggleMovieFollowController(
    private val handler: ToggleMovieFollowHandler,
) {

    @PostMapping("/movie/follow")
    fun toggleMovieFollow(
        @CurrentUser user: User,
        @RequestParam("movieId") movieId: Long,
        @RequestParam("q", required = false) query: String?
    ): String {
        ToggleMovieFollow(
            movieId = Movie.Id(movieId),
            userId = user.id!!,
        ).let(handler::handle)

        return if (!query.isNullOrBlank()) "redirect:/?q=${query}" else "redirect:/"
    }
}
