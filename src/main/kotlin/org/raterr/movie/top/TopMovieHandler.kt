package org.raterr.movie.top

import org.raterr.movie.Movie
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.movie.MovieRepository
import org.raterr.user.User
import org.springframework.stereotype.Service

data class TopMovie(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class RankedMovie(
    val rating: Rating,
    val movie: Movie
)

@Service
class TopMovieHandler(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(query: TopMovie): List<RankedMovie> =
        ratingRepository.findRankedByUserIdWithFilters(query.userId.value, query.category, query.limit, query.name)
            .map { it to it.movieId.let(Movie::Id).let(movieRepository::findById) }
            .map { RankedMovie(it.first, it.second!!) }
}
