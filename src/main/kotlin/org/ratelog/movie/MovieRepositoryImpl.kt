package org.ratelog.movie

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
class MovieRepositoryImpl(
    val movieDAO: MovieDAO,
    val movieFollowDAO: MovieFollowDAO,
): MovieRepository {
    override fun findById(id: Movie.Id): Movie? =
        id.value.let(movieDAO::findById).getOrNull()?.toDomain()

    override fun findByTmdbId(tmdbId: TmdbId): Movie? =
        tmdbId.value.let(movieDAO::findByTmdbId).getOrNull()?.toDomain()

    override fun save(movie: Movie) {
        movie.toEntity().let(movieDAO::save)
    }

    override fun findFollowedMovies(userId: User.Id): List<Movie> =
        movieDAO.findFollowedMovies(userId.value).map { it.toDomain() }

    override fun findAll(): List<Movie> =
        movieDAO.findAll().map { it.toDomain() }

    override fun isFollowed(userId: User.Id, movieId: Movie.Id): Boolean =
        movieFollowDAO.findByUserIdAndMovieId(userId.value, movieId.value).getOrNull() != null

    override fun toggleFollow(movieId: Movie.Id) {
        val currentUserId = UserDetailsService.getCurrentUser()?.id?.value ?: return

        val follow = movieFollowDAO.findByUserIdAndMovieId(currentUserId, movieId.value).getOrNull()

        if (follow == null) {
            MovieFollowEntity(
                userId = currentUserId,
                movieId = movieId.value,
            ).let(movieFollowDAO::save)
        } else follow.let(movieFollowDAO::delete)
    }

    private fun MovieEntity.toDomain(): Movie {
        val genres = genres?.split(',')?.mapNotNull(Genre::fromValue) ?: emptyList()

        return Movie(
            id = Movie.Id(id!!),
            tmdbId = TmdbId(tmdbId),
            title = Title(title),
            originalTitle = originalTitle?.let { Title(it) },
            overview = overview?.let { Overview(it) },
            releaseDate = releaseDate?.let { LocalDate.parse(it) },
            releaseYear = releaseYear,
            posterPath = posterPath?.let { Url(it) },
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres,
            status = status,
        )
    }

    private fun Movie.toEntity(): MovieEntity {
        return MovieEntity(
            id = id?.value,
            tmdbId = tmdbId.value,
            title = title.value,
            originalTitle = originalTitle?.value,
            overview = overview?.value,
            releaseDate = releaseDate?.toString(),
            releaseYear = releaseYear,
            posterPath = posterPath?.value,
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres.joinToString(",") { it.value },
            status = status,
        )
    }
}
