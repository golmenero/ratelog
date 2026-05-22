package org.raterr.tvshow.top

import org.raterr.UserId
import org.raterr.tvrating.TvRatingScoreService
import org.raterr.tvrating.TvRatingRepository
import org.raterr.tvshow.TvShow
import org.raterr.tvshow.TvShowRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

data class TopTvShow(
    val userId: UserId,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class RankedTvShow(
    val rank: Int,
    val ratingId: Long?,
    val tvShowId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val show: TvShow
)

@Service
class TopTvShowHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    fun handle(query: TopTvShow): List<RankedTvShow> =
        tvRatingRepository.findRankedByUserIdWithFilters(query.userId.value, query.category, query.limit, query.name)
            .map { ranked ->
                RankedTvShow(
                    rank = ranked.absRank,
                    ratingId = ranked.id,
                    tvShowId = ranked.tvShowId,
                    directing = ranked.directing,
                    cinematography = ranked.cinematography,
                    acting = ranked.acting,
                    soundtrack = ranked.soundtrack,
                    screenplay = ranked.screenplay,
                    createdAtEpochMs = ranked.createdAtEpochMs,
                    show = ranked.tvShowId.let(tvShowRepository::findById).getOrNull()!!
                )
            }
}
