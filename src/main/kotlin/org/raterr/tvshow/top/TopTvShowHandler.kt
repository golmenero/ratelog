package org.raterr.tvshow.top

import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvrating.TvRatingView
import org.raterr.user.User
import org.springframework.stereotype.Service

data class TopTvShow(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

@Service
class TopTvShowHandler(
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(query: TopTvShow): List<TvRatingView> =
        tvRatingRepository.findRankedByUserIdWithFilters(query.userId, query.category, query.limit, query.name)
}
