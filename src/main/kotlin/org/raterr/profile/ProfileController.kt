package org.raterr.profile

import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ProfileController(
    private val handler: ProfileHandler
) {

    @GetMapping("/profile")
    fun profilePage(
        @CurrentUser user: User,
        model: Model
    ): String {
        return user.id!!
            .let(::UserId)
            .let(::GetProfile)
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load profile.")
                    "search"
                },
                {
                    model.addAttribute("username", it.username)
                    model.addAttribute("email", it.email)
                    model.addAttribute("memberSince", it.memberSince)
                    model.addAttribute("releasedPremieres", it.premieres.released)
                    model.addAttribute("upcomingPremieres", it.premieres.upcoming)
                    model.addAttribute("noDatePremieres", it.premieres.noDate)
                    model.addAttribute("friends", it.friends)
                    "profile"
                }
            )
    }
}
