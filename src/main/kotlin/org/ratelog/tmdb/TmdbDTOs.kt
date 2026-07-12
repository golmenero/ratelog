package org.ratelog.tmdb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.ratelog.Genre
import org.ratelog.Status
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.Url
import org.ratelog.toLocalDate
import org.ratelog.movie.Movie
import org.ratelog.tvshow.TvShow
import java.time.LocalDate


@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSearchResponse(
    @JsonProperty("results") val results: List<TmdbMovieResponse> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbMovieResponse(
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("original_title") val originalTitle: String? = null,
    @JsonProperty("overview") val overview: String? = null,
    @JsonProperty("release_date") val releaseDate: String? = null,
    @JsonProperty("poster_path") val posterPath: String? = null,
    @JsonProperty("vote_average") val voteAverage: Double? = null,
    @JsonProperty("genres") val genres: List<TmdbGenreResponse> = emptyList(),
    @JsonProperty("status") val status: String? = null,
) {
    fun toDomain() = Movie(
        id = null,
        tmdbId = TmdbId(id),
        originalTitle = originalTitle?.let { Title(it) },
        releaseDate = releaseDate?.toLocalDate(),
        releaseYear = releaseDate?.takeIf { it.isNotBlank() }?.take(4)?.toIntOrNull(),
        posterPath = posterPath?.let { Url(it) },
        tmdbVoteAverage = voteAverage,
        genres = genres.mapNotNull { Genre.fromValue(it.name) },
        status = status?.let { Status.fromValue(it) },
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShowSearchResponse(
    @JsonProperty("results") val results: List<TmdbTvShowResponse> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShowResponse(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("original_name") val originalName: String? = null,
    @JsonProperty("overview") val overview: String? = null,
    @JsonProperty("first_air_date") val firstAirDate: String? = null,
    @JsonProperty("poster_path") val posterPath: String? = null,
    @JsonProperty("vote_average") val voteAverage: Double? = null,
    @JsonProperty("genres") val genres: List<TmdbGenreResponse> = emptyList(),
    @JsonProperty("status") val status: String? = null,
    @JsonProperty("seasons") val seasons: List<TmdbTvSeasonResponse> = emptyList()
) {
    fun toDomain() = TvShow(
        id = null,
        tmdbId = TmdbId(id),
        originalName = originalName?.let { Title(it) },
        firstAirDate = firstAirDate?.toLocalDate(),
        firstAirYear = firstAirDate?.takeIf { it.isNotBlank() }?.take(4)?.toIntOrNull(),
        posterPath = posterPath?.let { Url(it) },
        tmdbVoteAverage = voteAverage,
        genres = genres.mapNotNull { Genre.fromValue(it.name) },
        status = status?.let { Status.fromValue(it) },
        lastSeasonNumber = seasons.filter { it.seasonNumber > 0 }.maxByOrNull { it.seasonNumber }?.seasonNumber,
        lastSeasonAirDate = seasons.filter { it.seasonNumber > 0 }
            .maxByOrNull { it.seasonNumber }
            ?.airDate
            ?.toLocalDate(),
        nextSeasonAirDate = seasons.filter { it.seasonNumber > 0 }
            .mapNotNull { s -> s.airDate?.toLocalDate()?.let { d -> s to d } }
            .filter { (_, date) -> date > LocalDate.now() }
            .minByOrNull { (_, date) -> date }
            ?.second,
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvSeasonResponse(
    @JsonProperty("season_number") val seasonNumber: Int,
    @JsonProperty("episode_count") val episodeCount: Int? = null,
    @JsonProperty("air_date") val airDate: String? = null,
    @JsonProperty("overview") val overview: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbGenreResponse(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTranslationsResponse(
    @JsonProperty("translations") val translations: List<TmdbTranslationEntry> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTranslationEntry(
    @JsonProperty("iso_639_1") val iso6391: String,
    @JsonProperty("data") val data: TmdbTranslationData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTranslationData(
    @JsonProperty("title") val title: String? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("overview") val overview: String? = null,
)