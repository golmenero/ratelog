package org.ratelog.movie

import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Url
import org.ratelog.user.User
import java.time.LocalDate

data class Movie(
    val id: Id?,
    val tmdbId: TmdbId,
    val title: Title,
    val originalTitle: Title?,
    val overview: Overview?,
    val releaseDate: LocalDate?,
    val releaseYear: Int?,
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

interface MovieRepository {
    fun findById(id: Movie.Id): Movie?
    fun findByTmdbId(tmdbId: TmdbId): Movie?
    fun save(movie: Movie)
    fun findFollowedMovies(userId: User.Id): List<Movie>
}
