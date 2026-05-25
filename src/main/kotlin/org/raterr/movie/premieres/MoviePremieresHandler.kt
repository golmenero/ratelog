package org.raterr.movie.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.follow.FollowRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate

data class MoviePremieresQuery(val userId: User.Id)

data class MoviePremiereItem(
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
    private val followRepository: FollowRepository
) {
    fun handle(query: MoviePremieresQuery): Either<MoviePremieresHandlerError, MoviePremieres> = either {
        val follows = followRepository.findByUserId(query.userId.value)
            .filter { it.contentType == "movie" }

        val released = mutableListOf<MoviePremiereItem>()
        val upcoming = mutableListOf<MoviePremiereItem>()
        val noDate = mutableListOf<MoviePremiereItem>()
        val today = LocalDate.now()

        for (follow in follows) {
            val movie = tmdbClient.movieDetails(follow.contentTmdbId).bind()

            if (!movie.releaseDate.isNullOrBlank()) {
                val date = LocalDate.parse(movie.releaseDate)
                val item = MoviePremiereItem(
                    tmdbId = follow.contentTmdbId,
                    title = movie.title,
                    releaseDate = date,
                    posterPath = movie.posterPath,
                    isReleased = date <= today
                )
                if (item.isReleased) released.add(item) else upcoming.add(item)
            } else {
                noDate.add(
                    MoviePremiereItem(
                        tmdbId = follow.contentTmdbId,
                        title = movie.title,
                        releaseDate = today,
                        posterPath = movie.posterPath,
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
