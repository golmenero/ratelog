package org.ratelog.tvshow

import org.ratelog.Genre
import org.ratelog.Status
import org.ratelog.TmdbId
import org.ratelog.Title
import org.ratelog.Url
import org.ratelog.toLocalDate
import org.ratelog.user.User
import org.springframework.stereotype.Repository
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

    override fun save(show: TvShow): TvShow =
        show.toEntity().let(tvShowDAO::save).toDomain()

    override fun findFollowedTvShows(userId: User.Id): List<TvShow> =
        tvShowDAO.findFollowedTvShows(userId.value).map { it.toDomain() }

    override fun findActiveTvShows(): List<TvShow> =
        tvShowDAO.findActiveTvShows().map { it.toDomain() }

    override fun isFollowed(userId: User.Id, showId: TvShow.Id): Boolean =
        tvFollowDAO.findByUserIdAndTvShowId(userId.value, showId.value).getOrNull() != null

    override fun toggleFollow(userId: User.Id, showId: TvShow.Id) {
        val follow = tvFollowDAO.findByUserIdAndTvShowId(userId.value, showId.value).getOrNull()

        if (follow == null) {
            TvFollowEntity(
                userId = userId.value,
                tvShowId = showId.value,
            ).let(tvFollowDAO::save)
        } else follow.let(tvFollowDAO::delete)
    }

    private fun TvShowEntity.toDomain(): TvShow {
        val genres = genres?.split(',')?.mapNotNull { it.toIntOrNull()?.let { id -> Genre.fromTmdbId(id) } } ?: emptyList()

        return TvShow(
            id = TvShow.Id(id!!),
            tmdbId = TmdbId(tmdbId),
            originalName = Title(originalName),
            firstAirDate = firstAirDate?.toLocalDate(),
            firstAirYear = firstAirYear,
            posterPath = posterPath?.let { Url(it) },
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres,
            status = status?.let { Status.fromValue(it) },
            lastSeasonNumber = lastSeasonNumber,
            lastSeasonAirDate = lastSeasonAirDate?.toLocalDate(),
            nextSeasonAirDate = nextSeasonAirDate?.toLocalDate(),
        )
    }

    private fun TvShow.toEntity(): TvShowEntity {
        return TvShowEntity(
            id = id?.value,
            tmdbId = tmdbId.value,
            originalName = originalName.value,
            firstAirDate = firstAirDate?.toString(),
            firstAirYear = firstAirYear,
            posterPath = posterPath?.value,
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres.joinToString(",") { it.tmdbId.toString() },
            status = status?.value,
            lastSeasonNumber = lastSeasonNumber,
            lastSeasonAirDate = lastSeasonAirDate?.toString(),
            nextSeasonAirDate = nextSeasonAirDate?.toString(),
        )
    }
}
