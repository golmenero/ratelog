package org.ratelog.movie

import org.ratelog.Genre
import org.ratelog.Overview
import org.ratelog.Status
import org.ratelog.TmdbId
import org.ratelog.Title
import org.ratelog.Url
import org.ratelog.toLocalDate
import org.ratelog.user.User
import org.springframework.stereotype.Repository
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

    override fun save(movie: Movie): Movie =
        movie.toEntity().let(movieDAO::save).toDomain()

    override fun findFollowedMovies(userId: User.Id): List<Movie> =
        movieDAO.findFollowedMovies(userId.value).map { it.toDomain() }

    override fun findActiveMovies(): List<Movie> =
        movieDAO.findActiveMovies().map { it.toDomain() }

    override fun isFollowed(userId: User.Id, movieId: Movie.Id): Boolean =
        movieFollowDAO.findByUserIdAndMovieId(userId.value, movieId.value).getOrNull() != null

    override fun toggleFollow(userId: User.Id, movieId: Movie.Id) {
        val follow = movieFollowDAO.findByUserIdAndMovieId(userId.value, movieId.value).getOrNull()

        if (follow == null) {
            MovieFollowEntity(
                userId = userId.value,
                movieId = movieId.value,
            ).let(movieFollowDAO::save)
        } else follow.let(movieFollowDAO::delete)
    }

    private fun MovieEntity.toDomain(): Movie {
        val genres = genres?.split(',')?.mapNotNull(Genre::fromValue) ?: emptyList()

        return Movie(
            id = Movie.Id(id!!),
            tmdbId = TmdbId(tmdbId),
            originalTitle = originalTitle?.let { Title(it) },
            releaseDate = releaseDate?.toLocalDate(),
            releaseYear = releaseYear,
            posterPath = posterPath?.let { Url(it) },
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres,
            status = status?.let { Status.fromValue(it) },
        )
    }

    private fun Movie.toEntity(): MovieEntity {
        return MovieEntity(
            id = id?.value,
            tmdbId = tmdbId.value,
            originalTitle = originalTitle?.value,
            releaseDate = releaseDate?.toString(),
            releaseYear = releaseYear,
            posterPath = posterPath?.value,
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres.joinToString(",") { it.value },
            status = status?.value,
        )
    }
}
