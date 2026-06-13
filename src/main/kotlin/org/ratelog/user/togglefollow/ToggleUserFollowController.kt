package org.ratelog.user.togglefollow

import org.ratelog.Username
import org.ratelog.annotations.CurrentUser
import org.ratelog.user.User
import org.springframework.context.MessageSource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.Locale

@Controller
class ToggleUserFollowController(
    private val handler: ToggleUserFollowHandler,
    private val messageSource: MessageSource,
) {

    @PostMapping("/user/follow")
    fun toggleUserFollow(
        @CurrentUser user: User,
        @RequestParam("id") userId: Long,
        redirectAttributes: RedirectAttributes,
        locale: Locale
    ): String {
        ToggleUserFollow(
            followerId = user.id!!,
            followedId = userId.let(User::Id)
        ).let(handler::handle)
            .mapLeft {
                    error ->
                val message = when (error) {
                    is ToggleUserFollowHandlerError.UserNotFound -> messageSource.getMessage("error.user.not.found", null, locale)
                    is ToggleUserFollowHandlerError.CannotFollowYourself -> messageSource.getMessage("error.cannot.follow.yourself", null, locale)
                }
                redirectAttributes.addFlashAttribute("followError", message)
            }
        return "redirect:/profile/$userId"
    }
}
