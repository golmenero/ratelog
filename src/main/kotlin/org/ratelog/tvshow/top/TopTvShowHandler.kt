package org.ratelog.tvshow.top

import org.ratelog.Lang
import org.ratelog.Rank
import org.ratelog.tvshow.TvDescriptionRepository
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class TopTvShow(
    val userId: User.Id,
    val genreId: String?,
    val limit: Int = 10,
    val name: String?,
    val lang: Lang,
)

data class TopTvShowItem(
    val rank: Rank,
    val rating: TvRating,
    val tvShow: TvShow,
    val title: String,
)

@Service
class TopTvShowHandler(
    private val tvRatingRepository: TvRatingRepository,
    private val tvShowRepository: TvShowRepository,
    private val tvDescriptionRepository: TvDescriptionRepository,
) {
    @Transactional
    fun handle(query: TopTvShow): List<TopTvShowItem> =
        tvRatingRepository.findRankedByUserIdWithFilters(query.userId, query.genreId, query.limit, query.name)
            .mapNotNull { toItem(it, query.lang) }

    private fun toItem(item: Pair<Rank, TvRating>, lang: Lang) =
        tvShowRepository
            .findById(item.second.tvShowId)
            ?.let { show ->
                val description = tvDescriptionRepository.findByTmdbIdAndLang(show.tmdbId, lang)
                val title = description?.name?.value ?: show.originalName.value
                TopTvShowItem(rank = item.first, rating = item.second, tvShow = show, title = title)
            }
}
