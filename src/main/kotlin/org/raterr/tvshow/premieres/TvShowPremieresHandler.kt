package org.raterr.tvshow.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.tvshow.TvShowRepository
import org.raterr.tmdb.TmdbClient
import org.raterr.user.User
import org.springframework.stereotype.Service
import java.time.LocalDate

data class TvShowPremieresQuery(val userId: User.Id)

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
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(query: TvShowPremieresQuery): Either<TvShowPremieresHandlerError, TvShowPremieres> = either {
        val followedTvShows = query.userId.let(tvShowRepository::findFollowedTvShows)

        val released = mutableListOf<TvShowPremiereItem>()
        val upcoming = mutableListOf<TvShowPremiereItem>()
        val noDate = mutableListOf<TvShowPremiereItem>()
        val today = LocalDate.now()

        for (show in followedTvShows) {
            val tmdbShow = tmdbClient.tvShowDetails(show.tmdbId.value).bind()

            val latestSeason = tmdbShow.seasons
                .filter { it.seasonNumber > 0 }
                .maxByOrNull { it.seasonNumber }

            if (latestSeason != null) {
                if (!latestSeason.airDate.isNullOrBlank()) {
                    val date = LocalDate.parse(latestSeason.airDate)
                    val item = TvShowPremiereItem(
                        id = show.id!!.value,
                        tmdbId = show.tmdbId.value,
                        name = tmdbShow.name,
                        seasonNumber = latestSeason.seasonNumber,
                        releaseDate = date,
                        posterPath = tmdbShow.posterPath,
                        isReleased = date <= today
                    )
                    if (item.isReleased) released.add(item) else upcoming.add(item)
                } else {
                    noDate.add(
                        TvShowPremiereItem(
                            id = show.id!!.value,
                            tmdbId = show.tmdbId.value,
                            name = tmdbShow.name,
                            seasonNumber = latestSeason.seasonNumber,
                            releaseDate = today,
                            posterPath = tmdbShow.posterPath,
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
