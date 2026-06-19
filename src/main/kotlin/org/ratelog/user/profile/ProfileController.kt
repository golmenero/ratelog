package org.ratelog.user.profile

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProfileController(
    private val handler: ProfileHandler
) {
    @GetMapping("/profile")
    fun profilePage(@CurrentUser user: User): String = "redirect:/profile/${user.id!!.value}"

    @GetMapping("/profile/{id}")
    fun profilePage(
        @CurrentUser user: User,
        @PathVariable("id") userId: Long,
        model: Model
    ): String {
        return GetProfile(
            loggedUserId = user.id!!,
            userId = userId.let(User::Id),
        )
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "Could not load profile.")
                    "search"
                },
                {
                    model.addAttribute("followId", it.userId.value)
                    model.addAttribute("username", it.username.value)
                    model.addAttribute("email", it.email.value)
                    model.addAttribute("memberSince", it.memberSince)
                    model.addAttribute("currentLang", it.lang.name)
                    model.addAttribute("isFollowed", it.isFollowed)
                    model.addAttribute("isLoggedUser", it.userId == user.id)
                    model.addAttribute("ratings", it.ratings)
                    "profile"
                }
            )
    }
}
