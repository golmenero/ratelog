package org.ratelog.user.profile

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
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
            .let(::GetProfile)
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load profile.")
                    "search"
                },
                {
                    model.addAttribute("username", it.username.value)
                    model.addAttribute("email", it.email.value)
                    model.addAttribute("memberSince", it.memberSince)
                    "profile"
                }
            )
    }
}
