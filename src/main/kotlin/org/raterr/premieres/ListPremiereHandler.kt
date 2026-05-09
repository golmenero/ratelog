package org.raterr.premieres

import arrow.core.Either
import arrow.core.raise.either
import org.raterr.MediaType
import org.raterr.UserId
import org.raterr.tmdb.TmdbClient
import org.raterr.annotations.CurrentUser
import org.raterr.follow.FollowRepository
import org.raterr.user.User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

data class ListPremiere(val userId: UserId)

data class Premieres(
    val released: List<Item>,
    val upcoming: List<Item>,
    val noDate: List<Item>,
) {
    data class Item(
        val tmdbId: Int,
        val title: String,
        val releaseDate: LocalDate,
        val type: String,
        val posterPath: String?,
        val isReleased: Boolean,
        val hasDate: Boolean = true
    )
}


@Controller
class ListPremiereHandler(
    private val tmdbClient: TmdbClient,
    private val followRepository: FollowRepository,
) {

    fun handle(query: ListPremiere): Either<ListPremiereHandlerError, Premieres> = either {
        val follows = query.userId.value.let(followRepository::findByUserId)
        val released = mutableListOf<Premieres.Item>()
        val upcoming = mutableListOf<Premieres.Item>()
        val noDate = mutableListOf<Premieres.Item>()
        val today = LocalDate.now()

        for (follow in follows) {
            val (releaseDateStr, title, posterPath) = when (MediaType.valueOf(follow.contentType)) {
                MediaType.movie -> {
                    val movie = tmdbClient.movieDetails(follow.contentTmdbId).bind()
                    Triple(movie.releaseDate, movie.title, movie.posterPath)
                }
                MediaType.tvshow -> {
                    val show = tmdbClient.tvShowDetails(follow.contentTmdbId).bind()
                    Triple(show.firstAirDate, show.name, show.posterPath)
                }
            }

            if (!releaseDateStr.isNullOrBlank()) {
                val date = LocalDate.parse(releaseDateStr)
                val item = Premieres.Item(
                    tmdbId = follow.contentTmdbId,
                    title = title,
                    releaseDate = date,
                    type = follow.contentType,
                    posterPath = posterPath,
                    isReleased = date <= today
                )
                if (item.isReleased) released.add(item) else upcoming.add(item)
            } else {
                noDate.add(
                    Premieres.Item(
                        tmdbId = follow.contentTmdbId,
                        title = title,
                        releaseDate = today,
                        type = follow.contentType,
                        posterPath = posterPath,
                        isReleased = false,
                        hasDate = false
                    )
                )
            }
        }

        Premieres(
            released = released.sortedBy { it.releaseDate },
            upcoming = upcoming.sortedBy { it.releaseDate },
            noDate = noDate
        )
    }
}
