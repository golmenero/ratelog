package org.ratelog.user.profile

import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ProfileController(
    private val handler: ProfileHandler,
) {
    @GetMapping("/profile")
    fun profilePage(@CurrentUser user: User): String = "redirect:/profile/${user.id!!.value}"

    @GetMapping("/profile/{id}")
    fun profilePage(
        @CurrentUser user: User,
        @PathVariable("id") userId: Long,
        @RequestParam(value = "limit", defaultValue = "10") limit: Int,
        model: Model
    ): String {
        return GetProfile(
            loggedUserId = user.id!!,
            userId = userId.let(User::Id),
            limit = limit,
        )
            .let(handler::handle)
            .fold(
                {
                    model.addAttribute("error", "error.load.profile")
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
                    model.addAttribute("limit", limit)
                    "profile"
                }
            )
    }
}
