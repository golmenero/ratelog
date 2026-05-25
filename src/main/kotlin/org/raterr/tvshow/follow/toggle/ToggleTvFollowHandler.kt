package org.raterr.tvshow.follow.toggle

import org.raterr.tvshow.TvShow
import org.raterr.tvshow.follow.TvFollow
import org.raterr.tvshow.follow.TvFollowRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

data class ToggleTvFollow(
    val tvShowId: TvShow.Id,
    val userId: User.Id,
)

@Component
class ToggleTvFollowHandler(
    private val tvFollowRepository: TvFollowRepository,
) {
    fun handle(command: ToggleTvFollow) {
        val existingFollow = tvFollowRepository.findByUserIdAndTvShowId(
            command.userId.value,
            command.tvShowId.value,
        )

        if (existingFollow.isPresent) tvFollowRepository.delete(existingFollow.get())
        else {
            TvFollow(
                id = null,
                userId = command.userId.value,
                tvShowId = command.tvShowId.value
            ).let(tvFollowRepository::save)
        }
    }
}
