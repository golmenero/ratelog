package org.ratelog.movie.top

import org.ratelog.Rank
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieRepository
import org.ratelog.movie.rating.Rating
import org.ratelog.movie.rating.RatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    @Transactional
    fun handle(query: TopMovie): List<TopMovieItem> =
        ratingRepository.findRankedByUserIdWithFilters(query.userId, query.category, query.limit, query.name)
            .mapNotNull(::toItem)

    private fun toItem(item: Pair<Rank, Rating>) =
        movieRepository
            .findById(item.second.movieId)
            ?.let { TopMovieItem(rank = item.first, rating = item.second, movie = it) }
}
