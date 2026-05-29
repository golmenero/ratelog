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

    override fun save(show: TvShow): TvShow {
        val currentUserId = UserDetailsService.getCurrentUser()?.id?.value

        if (currentUserId != null && show.id != null) {
            val follow = tvFollowDAO.findByUserIdAndTvShowId(currentUserId, show.id.value).getOrNull()

            when {
                show.followed && follow == null -> {
                    TvFollowEntity(
                        userId = currentUserId,
                        tvShowId = show.id.value,
                    ).let(tvFollowDAO::save)
                }
                !show.followed && follow != null -> {
                    follow.let(tvFollowDAO::delete)
                }
            }
        }

        return show.toEntity().let(tvShowDAO::save).toDomain()
    }

    override fun findFollowedTvShows(userId: User.Id): List<TvShow> =
        tvShowDAO.findFollowedTvShows(userId.value).map { it.toDomain() }

    private fun TvShowEntity.toDomain(): TvShow {
        val genres = genres?.split(',')?.mapNotNull(Genre::fromValue) ?: emptyList()
        val currentUserId = UserDetailsService.getCurrentUser()?.id?.value
        val follow = currentUserId?.let { tvFollowDAO.findByUserIdAndTvShowId(it, id!!) }?.getOrNull()

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
            followed = follow != null,
            followedAtEpochMs = follow?.createdAtEpochMs
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
