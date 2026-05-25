package org.raterr.movie.rating.rank

import org.raterr.Rank
import org.raterr.movie.rating.Rating
import org.raterr.movie.rating.RatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class RankRating(val userId: User.Id)

@Service
class RankRatingHandler(
    private val ratingRepository: RatingRepository
) {
    @Transactional
    fun handle(command: RankRating) {
        val ratings = ratingRepository.findByUserIdOrderedByRank(command.userId).sortedByDescending { it.score }
        ratings.forEachIndexed { index, rating ->
            rating.id?.let { id ->
                ratingRepository.updateRank(id, Rank(index + 1))
            }
        }
    }
}