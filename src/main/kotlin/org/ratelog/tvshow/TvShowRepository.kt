package org.ratelog.tvshow

import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Url
import org.ratelog.user.User
import java.time.LocalDate

data class TvShow(
    val id: Id?,
    val tmdbId: TmdbId,
    val name: Title,
    val originalName: Title?,
    val overview: Overview?,
    val firstAirDate: LocalDate?,
    val firstAirYear: Int?,
    val posterPath: Url?,
    val tmdbVoteAverage: Double?,
    val genres: List<Genre>,
    val followed: Boolean = false,
    val followedAtEpochMs: Long? = null
) {
    data class Id(val value: Long)

    fun toggleFollow(now: Long) =
        if (followed) copy(followed = false, followedAtEpochMs = null)
        else copy(followed = true, followedAtEpochMs = now)
}

interface TvShowRepository {
    fun findById(id: TvShow.Id): TvShow?
    fun findByTmdbId(tmdbId: TmdbId): TvShow?
    fun save(show: TvShow): TvShow
    fun findFollowedTvShows(userId: User.Id): List<TvShow>
}
