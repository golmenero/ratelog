package org.raterr.rating

import org.raterr.rating.RatingScoreService.Companion.score
import org.raterr.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingRankService(
    private val ratingRepository: RatingRepository
) {
    @Transactional
    fun recalculateRanks(userId: User.Id) {
        val ratings = ratingRepository.findByUserIdOrderedByRank(userId)
            .sortedByDescending { score(it) }
        ratings.forEachIndexed { index, rating ->
            rating.id?.let { id ->
                ratingRepository.updateRank(id, Rating.Rank(index + 1))
            }
        }
    }
}
