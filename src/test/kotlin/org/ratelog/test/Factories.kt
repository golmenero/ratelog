package org.ratelog.test

import org.ratelog.*
import org.ratelog.movie.Movie
import org.ratelog.movie.rating.Rating
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.rating.SeasonRating
import org.ratelog.tvshow.rating.TvRating
import org.ratelog.tmdb.TmdbGenreResponse
import org.ratelog.tmdb.TmdbMovieResponse
import org.ratelog.tmdb.TmdbTvSeasonResponse
import org.ratelog.tmdb.TmdbTvShowResponse
import org.ratelog.user.User
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

object UserFactory {
    private val idGenerator = AtomicLong(1)

    fun aUser(
        id: Long? = null,
        username: String = "user${idGenerator.getAndIncrement()}",
        email: String = "user${idGenerator.get()}@example.com",
        passwordHash: String = "encoded_password",
        createdAtEpochMs: Long = System.currentTimeMillis(),
        lang: Lang = Lang("es"),
        followed: Boolean = false,
        followedAtEpochMs: Long? = null
    ) = User(
        id = id?.let { User.Id(it) },
        username = Username(username),
        email = Email(email),
        passwordHash = passwordHash,
        createdAtEpochMs = createdAtEpochMs,
        lang = lang,
        followed = followed,
        followedAtEpochMs = followedAtEpochMs
    )
}

object MovieFactory {
    private val idGenerator = AtomicLong(1)

    fun aMovie(
        id: Long? = null,
        tmdbId: Int = idGenerator.getAndIncrement().toInt(),
        title: String = "Movie $tmdbId",
        originalTitle: String? = null,
        overview: String? = null,
        releaseDate: LocalDate? = LocalDate.now().minusDays(1),
        releaseYear: Int? = null,
        posterPath: String? = null,
        tmdbVoteAverage: Double? = null,
        genres: List<Genre> = emptyList(),
        status: Status? = null,
    ) = Movie(
        id = id?.let { Movie.Id(it) },
        tmdbId = TmdbId(tmdbId),
        title = Title(title),
        originalTitle = originalTitle?.let { Title(it) },
        overview = overview?.let { Overview(it) },
        releaseDate = releaseDate,
        releaseYear = releaseYear,
        posterPath = posterPath?.let { Url(it) },
        tmdbVoteAverage = tmdbVoteAverage,
        genres = genres,
        status = status,
    )
}

object TvShowFactory {
    private val idGenerator = AtomicLong(1)

    fun aTvShow(
        id: Long? = null,
        tmdbId: Int = idGenerator.getAndIncrement().toInt(),
        name: String = "Show $tmdbId",
        originalName: String? = null,
        overview: String? = null,
        firstAirDate: LocalDate? = null,
        firstAirYear: Int? = null,
        posterPath: String? = null,
        tmdbVoteAverage: Double? = null,
        genres: List<Genre> = emptyList(),
        status: Status? = null,
        lastSeasonNumber: Int? = 3,
        lastSeasonAirDate: LocalDate? = LocalDate.now().minusDays(1),
        nextSeasonAirDate: LocalDate? = null,
    ) = TvShow(
        id = id?.let { TvShow.Id(it) },
        tmdbId = TmdbId(tmdbId),
        name = Title(name),
        originalName = originalName?.let { Title(it) },
        overview = overview?.let { Overview(it) },
        firstAirDate = firstAirDate,
        firstAirYear = firstAirYear,
        posterPath = posterPath?.let { Url(it) },
        tmdbVoteAverage = tmdbVoteAverage,
        genres = genres,
        status = status,
        lastSeasonNumber = lastSeasonNumber,
        lastSeasonAirDate = lastSeasonAirDate,
        nextSeasonAirDate = nextSeasonAirDate,
    )
}

object RatingFactory {
    private val idGenerator = AtomicLong(1)

    fun aRating(
        id: Long? = null,
        movieId: Movie.Id,
        userId: User.Id,
        directing: Double = 5.0,
        cinematography: Double = 5.0,
        acting: Double = 5.0,
        soundtrack: Double = 5.0,
        screenplay: Double = 5.0,
        createdAt: Instant = Instant.now()
    ) = Rating(
        id = id?.let { Rating.Id(it) },
        movieId = movieId,
        userId = userId,
        directing = Score(directing),
        cinematography = Score(cinematography),
        acting = Score(acting),
        soundtrack = Score(soundtrack),
        screenplay = Score(screenplay),
        createdAt = createdAt,
        score = Score((directing + cinematography + acting + soundtrack + screenplay) / 5.0)
    )
}

object TvRatingFactory {
    private val idGenerator = AtomicLong(1)

    fun aTvRating(
        id: Long? = null,
        tvShowId: TvShow.Id,
        userId: User.Id,
        seasonRatings: List<SeasonRating> = emptyList(),
        createdAt: Instant = Instant.now()
    ): TvRating {
        val score = if (seasonRatings.isEmpty()) null
        else Score(seasonRatings.map { it.score.value }.average())
        return TvRating(
            id = id?.let { TvRating.Id(it) },
            tvShowId = tvShowId,
            userId = userId,
            seasonRatings = seasonRatings,
            createdAt = createdAt,
            score = score
        )
    }

    fun aSeasonRating(
        id: Long? = null,
        tvShowId: TvShow.Id,
        seasonNumber: Int,
        userId: User.Id,
        directing: Double = 5.0,
        cinematography: Double = 5.0,
        acting: Double = 5.0,
        soundtrack: Double = 5.0,
        screenplay: Double = 5.0,
        createdAt: Instant = Instant.now()
    ) = SeasonRating(
        id = id?.let { SeasonRating.Id(it) },
        tvShowId = tvShowId,
        seasonNumber = SeasonNumber(seasonNumber),
        userId = userId,
        directing = Score(directing),
        cinematography = Score(cinematography),
        acting = Score(acting),
        soundtrack = Score(soundtrack),
        screenplay = Score(screenplay),
        createdAt = createdAt
    )
}