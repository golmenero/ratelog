package org.raterr.movie.top

import org.raterr.UserId
import org.raterr.rating.RatingScoreService
import org.raterr.rating.Rating
import org.raterr.rating.RatingRepository
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.springframework.stereotype.Controller
import kotlin.jvm.optionals.getOrNull

data class TopMovie(
    val userId: UserId,
    val year: Int?,
    val category: String?,
)

@Controller
class TopMovieHandler(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(query: TopMovie): List<Pair<Rating, Movie>> =
        ratingRepository.findByUserIdWithFilters(query.userId.value, query.year, query.category)
            .map { it to  it.movieId.let(movieRepository::findById).getOrNull()!! }
            .sortedByDescending { RatingScoreService.score(it.first) }
}
