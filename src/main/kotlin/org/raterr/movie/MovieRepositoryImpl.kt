package org.raterr.movie

import org.raterr.Genre
import org.raterr.Overview
import org.raterr.TmdbId
import org.raterr.Title
import org.raterr.Url
import org.raterr.user.User
import org.raterr.user.UserDetailsService
import org.springframework.stereotype.Repository
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull


@Repository
class MovieRepositoryImpl(
    val movieDAO: MovieDAO,
    val movieFollowDAO: MovieFollowDAO,
    private val userDetailsService: UserDetailsService,
): MovieRepository {
    override fun findById(id: Movie.Id): Movie? =
        id.value.let(movieDAO::findById).getOrNull()?.toDomain()

    override fun findByTmdbId(tmdbId: TmdbId): Movie? =
        tmdbId.value.let(movieDAO::findByTmdbId).getOrNull()?.toDomain()

    override fun save(movie: Movie): Movie {
        val currentUserId = userDetailsService.getCurrentUser()?.id?.value

        if (currentUserId != null && movie.id != null) {
            val follow = movieFollowDAO.findByUserIdAndMovieId(currentUserId, movie.id.value).getOrNull()

            when {
                movie.followed && follow == null -> {
                    MovieFollowEntity(
                        userId = currentUserId,
                        movieId = movie.id.value,
                    ).let(movieFollowDAO::save)
                }
                !movie.followed && follow != null -> {
                    follow.let(movieFollowDAO::delete)
                }
            }
        }

        return movie.toEntity().let(movieDAO::save).toDomain()
    }

    override fun findFollowedMovies(userId: User.Id): List<Movie> =
        movieDAO.findFollowedMovies(userId.value).map { it.toDomain() }


    private fun MovieEntity.toDomain(): Movie {
        val genres = genres?.split(',')?.map(Genre::valueOf) ?: emptyList()
        val currentUserId = userDetailsService.getCurrentUser()?.id?.value
        val follow = currentUserId?.let { movieFollowDAO.findByUserIdAndMovieId(it, id!!) }?.getOrNull()

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
            followed = follow != null,
            followedAtEpochMs = follow?.createdAtEpochMs
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
            genres = genres.joinToString(",")
        )
    }
}
