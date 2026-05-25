package org.raterr.rating

import org.raterr.Score
import org.raterr.movie.Movie
import org.raterr.user.User
import java.time.Instant

fun aRating(
    id: Rating.Id? = null,
    movieId: Movie.Id = Movie.Id(1),
    userId: User.Id = User.Id(1),
    directing: Double = 5.0,
    cinematography: Double = 5.0,
    acting: Double = 5.0,
    soundtrack: Double = 5.0,
    screenplay: Double = 5.0,
    createdAt: Instant = Instant.now(),
    rank: Rating.Rank = Rating.Rank(0)
): Rating =
    Rating(
        id = id,
        movieId = movieId,
        userId = userId,
        directing = Score(directing),
        cinematography = Score(cinematography),
        acting = Score(acting),
        soundtrack = Score(soundtrack),
        screenplay = Score(screenplay),
        createdAt = createdAt,
        rank = rank
    )
