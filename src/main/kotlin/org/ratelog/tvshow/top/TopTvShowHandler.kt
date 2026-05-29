package org.ratelog.tvshow.top

import org.ratelog.Rank
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service

data class TopTvShow(
    val userId: User.Id,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class TopTvShowItem(
    val rank: Rank,
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

    private fun toItem(item: Pair<Rank, TvRating>) = TopTvShowItem(
        rank = item.first,
        rating = item.second,
        tvShow = tvShowRepository.findById(item.second.tvShowId)!!
    )
}
