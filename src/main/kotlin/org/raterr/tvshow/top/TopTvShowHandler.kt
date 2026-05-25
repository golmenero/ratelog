package org.raterr.tvshow.top

import org.raterr.tvrating.TvRating
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.raterr.user.User
import org.springframework.stereotype.Service

data class TopTvShow(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class RankedTvShow(
    val rating: TvRating,
    val show: TvShow
)

@Service
class TopTvShowHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(query: TopTvShow): List<RankedTvShow> =
        tvRatingRepository.findRankedByUserIdWithFilters(query.userId.value, query.category, query.limit, query.name)
            .map { it to it.tvShowId.let(TvShow::Id).let(tvShowRepository::findById) }
            .map { RankedTvShow(it.first, it.second!!) }
}
