package org.raterr.tvshow.top

import org.raterr.UserId
import org.raterr.tvrating.TvRatingScoreService
import org.raterr.tvrating.TvRating
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.springframework.stereotype.Controller
import kotlin.jvm.optionals.getOrNull

data class TopTvShow(
    val userId: UserId,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

@Controller
class TopTvShowHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(query: TopTvShow): List<Pair<TvRating, TvShow>> =
        tvRatingRepository.findByUserIdWithFilters(query.userId.value, query.category, query.limit, query.name)
            .map { it to it.tvShowId.let(tvShowRepository::findById).getOrNull()!! }
}
