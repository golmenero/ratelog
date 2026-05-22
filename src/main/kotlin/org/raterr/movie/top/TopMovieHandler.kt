package org.raterr.movie.top

import org.raterr.UserId
import org.raterr.rating.RatingScoreService
import org.raterr.rating.RatingRepository
import org.raterr.movie.Movie
import org.raterr.movie.MovieRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

data class TopMovie(
    val userId: UserId,
    val category: String?,
    val limit: Int = 10,
    val name: String?
)

data class RankedMovie(
    val rank: Int,
    val ratingId: Long?,
    val movieId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val movie: Movie
)

@Service
class TopMovieHandler(
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
) {
    fun handle(query: TopMovie): List<RankedMovie> =
        ratingRepository.findRankedByUserIdWithFilters(query.userId.value, query.category, query.limit, query.name)
            .map { ranked ->
                RankedMovie(
                    rank = ranked.absRank,
                    ratingId = ranked.id,
                    movieId = ranked.movieId,
                    directing = ranked.directing,
                    cinematography = ranked.cinematography,
                    acting = ranked.acting,
                    soundtrack = ranked.soundtrack,
                    screenplay = ranked.screenplay,
                    createdAtEpochMs = ranked.createdAtEpochMs,
                    movie = ranked.movieId.let(movieRepository::findById).getOrNull()!!
                )
            }
}
