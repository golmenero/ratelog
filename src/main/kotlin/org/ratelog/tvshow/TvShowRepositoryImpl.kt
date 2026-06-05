package org.ratelog.tvshow

import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.TmdbId
import org.ratelog.Title
import org.ratelog.Url
import org.ratelog.user.User
import org.ratelog.user.UserDetailsService
import org.springframework.stereotype.Repository
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Repository
class TvShowRepositoryImpl(
    val tvShowDAO: TvShowDAO,
    val tvFollowDAO: TvFollowDAO,
) : TvShowRepository {
    override fun findById(id: TvShow.Id): TvShow? =
        id.value.let(tvShowDAO::findById).getOrNull()?.toDomain()

    override fun findByTmdbId(tmdbId: TmdbId): TvShow? =
        tmdbId.value.let(tvShowDAO::findByTmdbId).getOrNull()?.toDomain()

    override fun save(show: TvShow) {
        show.toEntity().let(tvShowDAO::save)
    }

    override fun findFollowedTvShows(userId: User.Id): List<TvShow> =
        tvShowDAO.findFollowedTvShows(userId.value).map { it.toDomain() }

    override fun isFollowed(userId: User.Id, showId: TvShow.Id): Boolean =
        tvFollowDAO.findByUserIdAndTvShowId(userId.value, showId.value).getOrNull() != null

    override fun toggleFollow(showId: TvShow.Id) {
        val currentUserId = UserDetailsService.getCurrentUser()?.id?.value ?: return

        val follow = tvFollowDAO.findByUserIdAndTvShowId(currentUserId, showId.value).getOrNull()

        if (follow == null) {
            TvFollowEntity(
                userId = currentUserId,
                tvShowId = showId.value,
            ).let(tvFollowDAO::save)
        } else follow.let(tvFollowDAO::delete)
    }

    private fun TvShowEntity.toDomain(): TvShow {
        val genres = genres?.split(',')?.mapNotNull(Genre::fromValue) ?: emptyList()

        return TvShow(
            id = TvShow.Id(id!!),
            tmdbId = TmdbId(tmdbId),
            name = Title(name),
            originalName = originalName?.let { Title(it) },
            overview = overview?.let { Overview(it) },
            firstAirDate = firstAirDate?.let { LocalDate.parse(it) },
            firstAirYear = firstAirYear,
            posterPath = posterPath?.let { Url(it) },
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres,
        )
    }

    private fun TvShow.toEntity(): TvShowEntity {
        return TvShowEntity(
            id = id?.value,
            tmdbId = tmdbId.value,
            name = name.value,
            originalName = originalName?.value,
            overview = overview?.value,
            firstAirDate = firstAirDate?.toString(),
            firstAirYear = firstAirYear,
            posterPath = posterPath?.value,
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres.joinToString(",") { it.value }
        )
    }
}
