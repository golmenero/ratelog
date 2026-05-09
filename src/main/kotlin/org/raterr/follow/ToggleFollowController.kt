package org.raterr.follow

import org.raterr.annotations.CurrentUser
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ToggleFollowController(
    private val followRepository: FollowRepository,
) {

    @PostMapping("/follow")
    fun toggleFollow(
        @CurrentUser user: User,
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("type") type: String,
        @RequestParam("q", required = false) query: String?
    ): String {
        val existingFollow = followRepository.findByUserIdAndContentTypeAndContentTmdbId(
            user.id!!, type, tmdbId
        )

        if (existingFollow.isPresent) followRepository.delete(existingFollow.get())
        else {
            Follow(
                userId = user.id,
                contentType = type,
                contentTmdbId = tmdbId
            ).let(followRepository::save)
        }

        return if (!query.isNullOrBlank()) "redirect:/?q=${query}" else "redirect:/"
    }
}
