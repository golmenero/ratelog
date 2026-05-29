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
        @RequestParam("username") username: String,
        @RequestParam(value = "from", required = false) from: String?,
        redirectAttributes: RedirectAttributes,
        locale: Locale
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
                            is ToggleUserFollowHandlerError.UserNotFound -> messageSource.getMessage("error.user.not.found", null, locale)
                            is ToggleUserFollowHandlerError.CannotFollowYourself -> messageSource.getMessage("error.cannot.follow.yourself", null, locale)
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
