package org.raterr.userfollow.toggleuser

import org.raterr.Username
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
        @RequestParam(value = "from", required = false) from: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        val redirectUrl = if (from == "community") "redirect:/community?username=$username" else "redirect:/community"
        return user.id?.let { userId ->
            ToggleUserFollow(
                followerId = userId,
                followedUsername = username.let(::Username),
                )
                .let(handler::handle)
                .fold(
                    { error ->
                        val message = when (error) {
                            is ToggleUserFollowHandlerError.UserNotFound -> "User not found"
                            is ToggleUserFollowHandlerError.CannotFollowYourself -> "Cannot follow yourself"
                        }
                        redirectAttributes.addFlashAttribute("followError", message)
                        redirectUrl
                    },
                    {
                        redirectUrl
                    }
                )
        } ?: redirectUrl
    }
}
