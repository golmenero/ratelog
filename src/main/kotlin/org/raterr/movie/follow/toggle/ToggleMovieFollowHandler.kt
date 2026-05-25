package org.raterr.movie.follow.toggle

import org.raterr.movie.Movie
import org.raterr.movie.follow.MovieFollow
import org.raterr.movie.follow.MovieFollowRepository
import org.raterr.user.User
import org.springframework.stereotype.Component

data class ToggleMovieFollow(
    val movieId: Movie.Id,
    val userId: User.Id,
)

@Component
class ToggleMovieFollowHandler(
    private val movieFollowRepository: MovieFollowRepository,
) {
    fun handle(command: ToggleMovieFollow) {
        val existingFollow = movieFollowRepository.findByUserIdAndMovieId(
            command.userId.value,
            command.movieId.value,
        )

        if (existingFollow.isPresent) movieFollowRepository.delete(existingFollow.get())
        else {
            MovieFollow(
                id = null,
                userId = command.userId.value,
                movieId = command.movieId.value
            ).let(movieFollowRepository::save)
        }
    }
}
