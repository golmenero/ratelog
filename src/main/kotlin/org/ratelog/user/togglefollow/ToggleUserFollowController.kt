package org.ratelog.user.togglefollow

import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
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
        @RequestParam("id") userId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        ToggleUserFollow(
            followerId = user.id!!,
            followedId = userId.let(User::Id)
        ).let(handler::handle)
            .mapLeft {
                    error ->
                val message = when (error) {
                    is ToggleUserFollowHandlerError.UserNotFound -> "error.user.not.found"
                    is ToggleUserFollowHandlerError.CannotFollowYourself -> "error.cannot.follow.yourself"
                }
                redirectAttributes.addFlashAttribute("followError", message)
            }
        return "redirect:/profile/$userId"
    }
}
