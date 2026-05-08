package org.raterr.search

import org.raterr.TmdbClient
import org.raterr.follow.FollowRepository
import org.raterr.movie.MovieRepository
import org.raterr.rating.RatingRepository
import org.raterr.tvshow.TvShowRepository
import org.raterr.tvrating.TvRatingRepository
import org.raterr.user.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Controller
class SearchController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val tvShowRepository: TvShowRepository,
    private val tvRatingRepository: TvRatingRepository,
    private val followRepository: FollowRepository,
    private val userService: UserService
) {

    @GetMapping("/")
    fun searchPage(@RequestParam("q", required = false) query: String?, model: Model): String {
        val currentUser = userService.getCurrentUser()
        if (!query.isNullOrBlank()) {
            val movies = searchMovies(query, currentUser).take(6)
            val shows = searchTvShows(query, currentUser).take(6)
            val interleaved = interleave(movies, shows)
            model.addAttribute("query", query)
            model.addAttribute("results", interleaved)
            model.addAttribute("currentUser", currentUser)
        }
        return "search"
    }

    private fun interleave(movies: List<SearchResultItem>, shows: List<SearchResultItem>): List<SearchResultItem> {
        val result = mutableListOf<SearchResultItem>()
        val maxSize = maxOf(movies.size, shows.size)
        for (i in 0 until maxSize) {
            if (i < movies.size) result.add(movies[i])
            if (i < shows.size) result.add(shows[i])
        }
        return result
    }

    private fun searchMovies(query: String, currentUser: org.raterr.user.User?): List<SearchResultItem> {
        return tmdbClient.searchMovies(query).map { tmdbMovie ->
            val movie = movieRepository.findByTmdbId(tmdbMovie.id).orElse(null)
            val ratings = if (movie != null) ratingRepository.findByMovieId(movie.id!!) else emptyList()
            val stats = calculateMovieStats(ratings)
            val isFollowed = currentUser?.let {
                followRepository.existsByUserIdAndContentTypeAndContentTmdbId(it.id!!, "movie", tmdbMovie.id)
            } ?: false
            val isReleased = tmdbMovie.releaseDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) <= LocalDate.now() } ?: false
            SearchResultItem(
                tmdbId = tmdbMovie.id,
                title = tmdbMovie.title,
                overview = tmdbMovie.overview,
                year = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                averageScore = stats.averageScore,
                ratingsCount = stats.ratingsCount,
                type = "movie",
                isFollowed = isFollowed,
                canRate = isReleased,
                canFollow = stats.ratingsCount == 0
            )
        }
    }

    private fun searchTvShows(query: String, currentUser: org.raterr.user.User?): List<SearchResultItem> {
        return tmdbClient.searchTvShows(query).map { tmdbShow ->
            val show = tvShowRepository.findByTmdbId(tmdbShow.id).orElse(null)
            val ratings = if (show != null) tvRatingRepository.findByTvShowId(show.id!!) else emptyList()
            val stats = calculateTvStats(ratings)
            val isFollowed = currentUser?.let {
                followRepository.existsByUserIdAndContentTypeAndContentTmdbId(it.id!!, "tvshow", tmdbShow.id)
            } ?: false
            SearchResultItem(
                tmdbId = tmdbShow.id,
                title = tmdbShow.name,
                overview = tmdbShow.overview,
                year = tmdbShow.firstAirDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbShow.posterPath,
                tmdbVoteAverage = tmdbShow.voteAverage,
                averageScore = stats.averageScore,
                ratingsCount = stats.ratingsCount,
                type = "tvshow",
                isFollowed = isFollowed,
                canRate = true,
                canFollow = stats.ratingsCount == 0
            )
        }
    }

    private fun calculateMovieStats(ratings: List<org.raterr.rating.Rating>): SearchScoreStats {
        if (ratings.isEmpty()) return SearchScoreStats(0.0, 0)
        val avg = ratings.map { (it.directing + it.cinematography + it.acting + it.soundtrack + it.screenplay) / 5.0 }.average()
        return SearchScoreStats(avg, ratings.size)
    }

    private fun calculateTvStats(ratings: List<org.raterr.tvrating.TvRating>): SearchScoreStats {
        if (ratings.isEmpty()) return SearchScoreStats(0.0, 0)
        val avg = ratings.map { (it.directing + it.cinematography + it.acting + it.soundtrack + it.screenplay) / 5.0 }.average()
        return SearchScoreStats(avg, ratings.size)
    }
}

data class SearchScoreStats(
    val averageScore: Double,
    val ratingsCount: Int
)

data class SearchResultItem(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val year: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val averageScore: Double,
    val ratingsCount: Int,
    val type: String,
    val isFollowed: Boolean = false,
    val canRate: Boolean = true,
    val canFollow: Boolean = true
)
