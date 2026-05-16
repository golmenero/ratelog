package org.raterr.community

import org.raterr.UserId
import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class UserSearchController(
    private val handler: UserSearchHandler
) {

    @GetMapping("/community")
    fun communityPage(
        @CurrentUser user: User?,
        @RequestParam("username", required = false) username: String?,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        if (!username.isNullOrBlank()) {
            UserSearchQuery(
                username = username,
                followerId = user?.id?.let(::UserId)
            ).let(handler::handle)
                .fold(
                    {
                        when (it) {
                            is UserSearchHandlerError.EmptyQuery -> {
                                redirectAttributes.addFlashAttribute("error", "Query cannot be empty")
                            }
                            is UserSearchHandlerError.NoUsersFound -> {
                                redirectAttributes.addFlashAttribute("error", "No users found for '${it.username}'")
                            }
                        }
                    },
                    {
                        model.addAttribute("users", it)
                        model.addAttribute("searchedUsername", username)
                    }
                )
        }
        return "community"
    }
}
