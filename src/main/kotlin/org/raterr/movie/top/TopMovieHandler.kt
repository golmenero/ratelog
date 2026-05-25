package org.raterr.movie.top

import org.raterr.movie.rating.RatingRepository
import org.raterr.movie.rating.RatingView
import org.raterr.user.User
import org.springframework.stereotype.Service

data class TopMovie(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

@Service
class TopMovieHandler(
    private val ratingRepository: RatingRepository,
) {
    fun handle(query: TopMovie): List<RatingView> =
        ratingRepository.findRankedByUserIdWithFilters(query.userId, query.category, query.limit, query.name)
}
