package org.raterr.follow.toggle

import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class ToggleUserFollowController(
    private val handler: ToggleUserFollowHandler,
) {

    @PostMapping("/user/follow")
    fun toggleUserFollow(
        @CurrentUser user: User,
        @RequestParam("username") username: String,
        redirectAttributes: RedirectAttributes
    ): String {
        return user.id?.let { userId ->
            ToggleUserFollow(UserId(userId), username)
                .let(handler::handle)
                .fold(
                    { error ->
                        val message = when (error) {
                            is ToggleUserFollowHandlerError.UserNotFound -> "User not found"
                            is ToggleUserFollowHandlerError.CannotFollowYourself -> "Cannot follow yourself"
                        }
                        redirectAttributes.addFlashAttribute("followError", message)
                        "redirect:/profile"
                    },
                    {
                        "redirect:/profile"
                    }
                )
        } ?: "redirect:/profile"
    }
}
