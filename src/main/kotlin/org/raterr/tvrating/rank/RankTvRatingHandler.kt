package org.raterr.tvrating.rank

import org.raterr.tvrating.TvRating
import org.raterr.tvrating.TvRatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class RankTvRating(val userId: User.Id)

@Service
class RankTvRatingHandler(
    private val tvRatingRepository: TvRatingRepository
) {
    @Transactional
    fun handle(command: RankTvRating) {
        val ratings = tvRatingRepository.findByUserIdOrderedByRank(command.userId).sortedByDescending { it.score }
        ratings.forEachIndexed { index, rating ->
            rating.id?.let { id ->
                tvRatingRepository.updateRank(id, TvRating.Rank(index + 1))
            }
        }
    }
}