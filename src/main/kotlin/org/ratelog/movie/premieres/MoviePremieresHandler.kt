package org.ratelog.movie.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.movie.MovieRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate
import org.springframework.transaction.annotation.Transactional

data class MoviePremieresQuery(val userId: User.Id)

data class MoviePremiereItem(
    val id: Long,
    val tmdbId: Int,
    val title: String,
    val releaseDate: LocalDate,
    val posterPath: String?,
    val isReleased: Boolean,
    val hasDate: Boolean = true
)

data class MoviePremieres(
    val released: List<MoviePremiereItem>,
    val upcoming: List<MoviePremiereItem>,
    val noDate: List<MoviePremiereItem>
)

@Service
class MoviePremieresHandler(
    private val movieRepository: MovieRepository,
) {
    @Transactional
    fun handle(query: MoviePremieresQuery): Either<MoviePremieresHandlerError, MoviePremieres> = either {
        val followedMovies = query.userId.let(movieRepository::findFollowedMovies)

        val released = mutableListOf<MoviePremiereItem>()
        val upcoming = mutableListOf<MoviePremiereItem>()
        val noDate = mutableListOf<MoviePremiereItem>()
        val today = LocalDate.now()

        for (movie in followedMovies) {
            if (movie.releaseDate != null) {
                val item = MoviePremiereItem(
                    id = movie.id!!.value,
                    tmdbId = movie.tmdbId.value,
                    title = movie.title.value,
                    releaseDate = movie.releaseDate,
                    posterPath = movie.posterPath?.value,
                    isReleased = movie.releaseDate <= today
                )
                if (item.isReleased) released.add(item) else upcoming.add(item)
            } else {
                noDate.add(
                    MoviePremiereItem(
                        id = movie.id!!.value,
                        tmdbId = movie.tmdbId.value,
                        title = movie.title.value,
                        releaseDate = today,
                        posterPath = movie.posterPath?.value,
                        isReleased = false,
                        hasDate = false
                    )
                )
            }
        }

        MoviePremieres(
            released = released.sortedBy { it.releaseDate },
            upcoming = upcoming.sortedBy { it.releaseDate },
            noDate = noDate
        )
    }
}
