package org.raterr.tvrating

import org.raterr.tvrating.TvRatingScoreService.Companion.score
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TvRatingRankService(
    private val tvRatingRepository: TvRatingRepository
) {
    @Transactional
    fun recalculateRanks(userId: Long) {
        val ratings = tvRatingRepository.findByUserIdOrderedByRank(userId)
            .sortedByDescending { score(it) }
        ratings.forEachIndexed { index, rating ->
            tvRatingRepository.updateRank(rating.id!!, index + 1)
        }
    }
}
