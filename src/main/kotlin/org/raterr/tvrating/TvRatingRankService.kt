package org.raterr.tvrating

import org.raterr.tvrating.TvRatingScoreService.Companion.score
import org.raterr.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TvRatingRankService(
    private val tvRatingRepository: TvRatingRepository
) {
    @Transactional
    fun recalculateRanks(userId: User.Id) {
        val ratings = tvRatingRepository.findByUserIdOrderedByRank(userId)
            .sortedByDescending { score(it) }
        ratings.forEachIndexed { index, rating ->
            rating.id?.let { id ->
                tvRatingRepository.updateRank(id, TvRating.Rank(index + 1))
            }
        }
    }
}
