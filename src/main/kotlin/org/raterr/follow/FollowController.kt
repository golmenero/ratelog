package org.raterr.follow

import org.raterr.user.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class FollowController(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository
) {

    @PostMapping("/follow")
    fun toggleFollow(
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("type") type: String,
        @RequestParam("q", required = false) query: String?,
        authentication: Authentication?
    ): String {
        if (authentication == null) {
            return "redirect:/login"
        }

        val user = userRepository.findByUsername(authentication.name)
            .orElseThrow { IllegalStateException("User not found") }

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
