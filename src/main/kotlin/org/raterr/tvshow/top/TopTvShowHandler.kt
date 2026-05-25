package org.raterr.tvshow.top

import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvshow.rating.TvRating
import org.raterr.tvshow.rating.TvRatingRepository
import org.raterr.user.User
import org.springframework.stereotype.Service

data class TopTvShow(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class TopTvShowItem(
    val rating: TvRating,
    val tvShow: TvShow,
)

@Service
class TopTvShowHandler(
    private val tvRatingRepository: TvRatingRepository,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(query: TopTvShow): List<TopTvShowItem> =
        tvRatingRepository.findRankedByUserIdWithFilters(query.userId, query.category, query.limit, query.name)
            .map(::toItem)

    private fun toItem(tvRating: TvRating) = TopTvShowItem(
        rating = tvRating,
        tvShow = tvShowRepository.findById(tvRating.tvShowId)!!
    )
}
