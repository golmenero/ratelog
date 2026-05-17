package org.raterr.tvshow.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.UserId
import org.raterr.follow.FollowRepository
import org.raterr.tmdb.TmdbClient
import org.springframework.stereotype.Service
import java.time.LocalDate

data class TvShowPremieresQuery(val userId: UserId)

data class TvShowPremiereItem(
    val tmdbId: Int,
    val name: String,
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
    private val followRepository: FollowRepository
) {
    fun handle(query: TvShowPremieresQuery): Either<TvShowPremieresHandlerError, TvShowPremieres> = either {
        val follows = followRepository.findByUserId(query.userId.value)
            .filter { it.contentType == "tvshow" }

        val released = mutableListOf<TvShowPremiereItem>()
        val upcoming = mutableListOf<TvShowPremiereItem>()
        val noDate = mutableListOf<TvShowPremiereItem>()
        val today = LocalDate.now()

        for (follow in follows) {
            val show = tmdbClient.tvShowDetails(follow.contentTmdbId).bind()

            if (!show.firstAirDate.isNullOrBlank()) {
                val date = LocalDate.parse(show.firstAirDate)
                val item = TvShowPremiereItem(
                    tmdbId = follow.contentTmdbId,
                    name = show.name,
                    releaseDate = date,
                    posterPath = show.posterPath,
                    isReleased = date <= today
                )
                if (item.isReleased) released.add(item) else upcoming.add(item)
            } else {
                noDate.add(
                    TvShowPremiereItem(
                        tmdbId = follow.contentTmdbId,
                        name = show.name,
                        releaseDate = today,
                        posterPath = show.posterPath,
                        isReleased = false,
                        hasDate = false
                    )
                )
            }
        }

        TvShowPremieres(
            released = released.sortedBy { it.releaseDate },
            upcoming = upcoming.sortedBy { it.releaseDate },
            noDate = noDate
        )
    }
}
