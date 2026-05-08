package org.raterr.usecases.movie.rating

import org.raterr.TmdbClient
import org.raterr.TmdbMovie
import org.raterr.usecases.follow.FollowRepository
import org.raterr.usecases.movie.Movie
import org.raterr.usecases.movie.MovieRepository
import org.raterr.usecases.user.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AddRatingController(
    private val tmdbClient: TmdbClient,
    private val movieRepository: MovieRepository,
    private val ratingRepository: RatingRepository,
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository
) {

    @PostMapping("/movie/rate")
    fun saveRating(
        @RequestParam("tmdbId") tmdbId: Int,
        @RequestParam("directing") directing: Double,
        @RequestParam("cinematography") cinematography: Double,
        @RequestParam("acting") acting: Double,
        @RequestParam("soundtrack") soundtrack: Double,
        @RequestParam("screenplay") screenplay: Double,
        redirectAttributes: RedirectAttributes
    ): String {
        return try {
            listOf(
                "directing" to directing,
                "cinematography" to cinematography,
                "acting" to acting,
                "soundtrack" to soundtrack,
                "screenplay" to screenplay
            ).forEach { (field, value) ->
                require(value >= 1.0 && value <= 10.0) { "Field $field must be between 1 and 10" }
            }

            val authentication = SecurityContextHolder.getContext().authentication
            val username = authentication.name
            val user = userRepository.findByUsername(username)
                .orElseThrow { IllegalArgumentException("User not found") }

            val movie = movieRepository.findByTmdbId(tmdbId).orElse(null)
                ?: upsertMovie(tmdbClient.movieDetails(tmdbId))

            val existingRating = ratingRepository.findByMovieIdAndUserId(movie.id!!, user.id!!).firstOrNull()
            if (existingRating != null) {
                redirectAttributes.addAttribute("id", tmdbId)
                redirectAttributes.addFlashAttribute("error", "A rating already exists for this movie.")
                return "redirect:/movie/rate"
            }

            val newRating = Rating(
                movieId = movie.id!!,
                userId = user.id!!,
                directing = directing,
                cinematography = cinematography,
                acting = acting,
                soundtrack = soundtrack,
                screenplay = screenplay,
                createdAtEpochMs = System.currentTimeMillis()
            )
            ratingRepository.save(newRating)

            followRepository.findByUserIdAndContentTypeAndContentTmdbId(user.id!!, "movie", tmdbId).ifPresent { follow ->
                followRepository.delete(follow)
            }

            redirectAttributes.addFlashAttribute("success", "Rating saved successfully.")
            "redirect:/movie/top"
        } catch (e: IllegalArgumentException) {
            redirectAttributes.addAttribute("id", tmdbId)
            redirectAttributes.addFlashAttribute("error", e.message)
            "redirect:/movie/rate"
        } catch (e: Exception) {
            redirectAttributes.addAttribute("id", tmdbId)
            redirectAttributes.addFlashAttribute("error", "Could not save the rating.")
            "redirect:/movie/rate"
        }
    }

    private fun upsertMovie(tmdbMovie: TmdbMovie): Movie {
        val existing = movieRepository.findByTmdbId(tmdbMovie.id).orElse(null)

        return if (existing != null) {
            val updated = existing.copy(
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = tmdbMovie.genres.joinToString(",") { it.name }
            )
            movieRepository.save(updated)
        } else {
            val newMovie = Movie(
                tmdbId = tmdbMovie.id,
                title = tmdbMovie.title,
                originalTitle = tmdbMovie.originalTitle,
                overview = tmdbMovie.overview,
                releaseDate = tmdbMovie.releaseDate,
                releaseYear = tmdbMovie.releaseDate?.take(4)?.toIntOrNull(),
                posterPath = tmdbMovie.posterPath,
                tmdbVoteAverage = tmdbMovie.voteAverage,
                genres = tmdbMovie.genres.joinToString(",") { it.name }
            )
            movieRepository.save(newMovie)
        }
    }
}
