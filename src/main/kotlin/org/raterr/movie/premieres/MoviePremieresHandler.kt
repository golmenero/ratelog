package org.raterr.movie.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.movie.MovieRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate

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
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
) {
    fun handle(query: MoviePremieresQuery): Either<MoviePremieresHandlerError, MoviePremieres> = either {
        val followedMovies = query.userId.let(movieRepository::findFollowedMovies)

        val released = mutableListOf<MoviePremiereItem>()
        val upcoming = mutableListOf<MoviePremiereItem>()
        val noDate = mutableListOf<MoviePremiereItem>()
        val today = LocalDate.now()

        for (movie in followedMovies) {
            val tmdbMovie = tmdbClient.movieDetails(movie.tmdbId.value).bind()

            if (!tmdbMovie.releaseDate.isNullOrBlank()) {
                val date = LocalDate.parse(tmdbMovie.releaseDate)
                val item = MoviePremiereItem(
                    id = movie.id!!.value,
                    tmdbId = movie.tmdbId.value,
                    title = tmdbMovie.title,
                    releaseDate = date,
                    posterPath = tmdbMovie.posterPath,
                    isReleased = date <= today
                )
                if (item.isReleased) released.add(item) else upcoming.add(item)
            } else {
                noDate.add(
                    MoviePremiereItem(
                        id = movie.id!!.value,
                        tmdbId = movie.tmdbId.value,
                        title = tmdbMovie.title,
                        releaseDate = today,
                        posterPath = tmdbMovie.posterPath,
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
