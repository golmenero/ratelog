package org.raterr.follow.toggle

import org.raterr.MediaType
import org.raterr.TmdbId
import org.raterr.follow.Follow
import org.raterr.follow.FollowRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

data class ToggleFollow(
    val tmdbId: TmdbId,
    val userId: User.Id,
    val type: MediaType,
)

@Component
class ToggleFollowHandler(
    private val followRepository: FollowRepository,
) {
    fun handle(command: ToggleFollow) {
        val existingFollow = followRepository.findByUserIdAndContentTypeAndContentTmdbId(
            command.userId.value,
            command.type.name,
            command.tmdbId.value,
        )

        if (existingFollow.isPresent) followRepository.delete(existingFollow.get())
        else {
            Follow(
                id = null,
                userId = command.userId.value,
                contentType = command.type.name,
                contentTmdbId = command.tmdbId.value
            ).let(followRepository::save)
        }
    }
}