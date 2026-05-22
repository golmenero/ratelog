package org.raterr.rating

import org.raterr.rating.RatingScoreService.Companion.score
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RatingRankService(
    private val ratingRepository: RatingRepository
) {
    @Transactional
    fun recalculateRanks(userId: Long) {
        val ratings = ratingRepository.findByUserIdOrderedByRank(userId)
            .sortedByDescending { score(it) }
        ratings.forEachIndexed { index, rating ->
            ratingRepository.updateRank(rating.id!!, index + 1)
        }
    }
}
