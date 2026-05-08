package org.raterr.follow

import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ToggleFollowController(
    private val followRepository: FollowRepository,
    private val userService: UserService
) {

    @PostMapping("/follow")
    fun toggleFollow(
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("type") type: String,
        @RequestParam("q", required = false) query: String?
    ): String {
        val user = userService.getCurrentUser() ?: return "redirect:/login"

        val existingFollow = followRepository.findByUserIdAndContentTypeAndContentTmdbId(
            user.id!!, type, tmdbId
        )

        if (existingFollow.isPresent) {
            followRepository.delete(existingFollow.get())
        } else {
            val follow = Follow(
                userId = user.id!!,
                contentType = type,
                contentTmdbId = tmdbId
            )
            followRepository.save(follow)
        }

        return if (!query.isNullOrBlank()) "redirect:/?q=${query}" else "redirect:/"
    }
}
