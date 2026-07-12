package org.ratelog.movie.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.movie.MovieDescriptionRepository
import org.ratelog.movie.MovieRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate
import org.springframework.transaction.annotation.Transactional

data class MoviePremieresQuery(val userId: User.Id, val lang: Lang)

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
    private val movieDescriptionRepository: MovieDescriptionRepository,
) {
    @Transactional
    fun handle(query: MoviePremieresQuery): Either<MoviePremieresHandlerError, MoviePremieres> = either {
        val followedMovies = query.userId.let(movieRepository::findFollowedMovies)

        val released = mutableListOf<MoviePremiereItem>()
        val upcoming = mutableListOf<MoviePremiereItem>()
        val noDate = mutableListOf<MoviePremiereItem>()
        val today = LocalDate.now()

        for (movie in followedMovies) {
            val description = movieDescriptionRepository.findByTmdbIdAndLang(movie.tmdbId, query.lang)
            val title = description?.title?.value ?: movie.originalTitle.value

            if (movie.releaseDate != null) {
                val item = MoviePremiereItem(
                    id = movie.id!!.value,
                    tmdbId = movie.tmdbId.value,
                    title = title,
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
                        title = title,
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
