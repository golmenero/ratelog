package org.ratelog.tvshow.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.tvshow.TvDescriptionRepository
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate
import org.springframework.transaction.annotation.Transactional

data class TvShowPremieresQuery(val userId: User.Id, val lang: Lang)

data class TvShowPremiereItem(
    val id: Long,
    val tmdbId: Int,
    val name: String,
    val seasonNumber: Int,
    val releaseDate: LocalDate,
    val posterPath: String?,
    val isReleased: Boolean,
    val hasDate: Boolean = true
)

data class TvShowPremieres(
    val released: List<TvShowPremiereItem>,
    val upcoming: List<TvShowPremiereItem>,
    val noDate: List<TvShowPremiereItem>
)

@Service
class TvShowPremieresHandler(
    private val tvShowRepository: TvShowRepository,
    private val tvDescriptionRepository: TvDescriptionRepository,
) {
    @Transactional
    fun handle(query: TvShowPremieresQuery): Either<TvShowPremieresHandlerError, TvShowPremieres> = either {
        val followedTvShows = query.userId.let(tvShowRepository::findFollowedTvShows)

        val released = mutableListOf<TvShowPremiereItem>()
        val upcoming = mutableListOf<TvShowPremiereItem>()
        val noDate = mutableListOf<TvShowPremiereItem>()
        val today = LocalDate.now()

        for (show in followedTvShows) {
            val description = tvDescriptionRepository.findByTmdbIdAndLang(show.tmdbId, query.lang)
            val name = description?.name?.value ?: show.originalName?.value ?: ""

            if (show.lastSeasonNumber != null) {
                if (show.lastSeasonAirDate != null) {
                    val item = TvShowPremiereItem(
                        id = show.id!!.value,
                        tmdbId = show.tmdbId.value,
                        name = name,
                        seasonNumber = show.lastSeasonNumber,
                        releaseDate = show.lastSeasonAirDate,
                        posterPath = show.posterPath?.value,
                        isReleased = show.lastSeasonAirDate <= today
                    )
                    if (item.isReleased) released.add(item) else upcoming.add(item)
                } else {
                    noDate.add(
                        TvShowPremiereItem(
                            id = show.id!!.value,
                            tmdbId = show.tmdbId.value,
                            name = name,
                            seasonNumber = show.lastSeasonNumber,
                            releaseDate = today,
                            posterPath = show.posterPath?.value,
                            isReleased = false,
                            hasDate = false
                        )
                    )
                }
            }
        }

        TvShowPremieres(
            released = released.sortedBy { it.releaseDate },
            upcoming = upcoming.sortedBy { it.releaseDate },
            noDate = noDate
        )
    }
}
