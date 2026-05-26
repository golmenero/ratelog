package org.raterr.movie.top

import org.raterr.Rank
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.raterr.movie.rating.Rating
import org.raterr.movie.rating.RatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Service

data class TopMovie(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class TopMovieItem(
    val rank: Rank,
    val rating: Rating,
    val movie: Movie,
)

@Service
class TopMovieHandler(
    private val ratingRepository: RatingRepository,
    private val movieRepository: MovieRepository,
) {
    fun handle(query: TopMovie): List<TopMovieItem> =
        ratingRepository.findRankedByUserIdWithFilters(query.userId, query.category, query.limit, query.name)
            .map(::toItem)

    private fun toItem(item: Pair<Rank, Rating>) = TopMovieItem(
        rank = item.first,
        rating = item.second,
        movie = movieRepository.findById(item.second.movieId)!!
    )
}
