package org.raterr.premieres

import org.raterr.UserId
import org.raterr.tmdb.TmdbClient
import org.raterr.annotations.CurrentUser
import org.raterr.follow.FollowRepository
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

@Controller
class ListPremiereController(
    private val handler: ListPremiereHandler,
) {

    @GetMapping("/premieres")
    fun premieresPage(
        @CurrentUser user: User,
        model: Model
    ): String {
        user.id!!
            .let(::UserId)
            .let(::ListPremiere)
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load the movie.")
                    return "rate"
                },
                {
                    model.addAttribute("releasedPremieres", it.released)
                    model.addAttribute("upcomingPremieres", it.upcoming)
                    model.addAttribute("noDatePremieres", it.noDate)
                    return "premieres"
                },
            )


    }
}
