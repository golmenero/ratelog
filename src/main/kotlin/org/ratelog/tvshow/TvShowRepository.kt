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
) {
    data class Id(val value: Long)
}

interface TvShowRepository {
    fun findById(id: TvShow.Id): TvShow?
    fun findByTmdbId(tmdbId: TmdbId): TvShow?
    fun save(show: TvShow)
    fun findFollowedTvShows(userId: User.Id): List<TvShow>
    fun isFollowed(userId: User.Id, showId: TvShow.Id): Boolean
    fun toggleFollow(showId: TvShow.Id)
}
