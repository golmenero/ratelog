package org.raterr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class TmdbClient(
    @Value("\${raterr.tmdb.api-key}")
    private val apiKey: String,

    @Value("\${raterr.tmdb.base-url}")
    private val baseUrl: String = "https://api.themoviedb.org/3"
) {
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()

    @Cacheable(value = ["tmdb-search-movies"], key = "#query")
    fun searchMovies(query: String): List<TmdbMovie> {
        if (query.isBlank()) return emptyList()
        requireApiKey()

        return restClient.get()
            .uri { builder ->
                builder.path("/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "en-US")
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .body(TmdbSearchResponse::class.java)
            ?.results
            ?: emptyList()
    }

    @Cacheable(value = ["tmdb-movie-details"], key = "#tmdbId")
    fun movieDetails(tmdbId: Int): TmdbMovie {
        requireApiKey()

        return restClient.get()
            .uri { builder ->
                builder.path("/movie/{id}")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "en-US")
                    .build(tmdbId)
            }
            .retrieve()
            .body(TmdbMovie::class.java)
            ?: throw RuntimeException("Could not fetch movie details")
    }

    @Cacheable(value = ["tmdb-search-tvshows"], key = "#query")
    fun searchTvShows(query: String): List<TmdbTvShow> {
        if (query.isBlank()) return emptyList()
        requireApiKey()

        return restClient.get()
            .uri { builder ->
                builder.path("/search/tv")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "en-US")
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .body(TmdbTvShowSearchResponse::class.java)
            ?.results
            ?: emptyList()
    }

    @Cacheable(value = ["tmdb-tvshow-details"], key = "#tmdbId")
    fun tvShowDetails(tmdbId: Int): TmdbTvShow {
        requireApiKey()

        return restClient.get()
            .uri { builder ->
                builder.path("/tv/{id}")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "en-US")
                    .build(tmdbId)
            }
            .retrieve()
            .body(TmdbTvShow::class.java)
            ?: throw RuntimeException("Could not fetch TV show details")
    }

    private fun requireApiKey() {
        require(apiKey.isNotBlank()) {
            "TMDB_API_KEY is missing. Set the environment variable before using search or details."
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSearchResponse(
    @JsonProperty("results")
    val results: List<TmdbMovie> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbMovie(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("original_title")
    val originalTitle: String? = null,
    @JsonProperty("overview")
    val overview: String? = null,
    @JsonProperty("release_date")
    val releaseDate: String? = null,
    @JsonProperty("poster_path")
    val posterPath: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null,
    @JsonProperty("genres")
    val genres: List<TmdbGenre> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShowSearchResponse(
    @JsonProperty("results")
    val results: List<TmdbTvShow> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbTvShow(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("original_name")
    val originalName: String? = null,
    @JsonProperty("overview")
    val overview: String? = null,
    @JsonProperty("first_air_date")
    val firstAirDate: String? = null,
    @JsonProperty("poster_path")
    val posterPath: String? = null,
    @JsonProperty("vote_average")
    val voteAverage: Double? = null,
    @JsonProperty("genres")
    val genres: List<TmdbGenre> = emptyList(),
    @JsonProperty("status")
    val status: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbGenre(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("name")
    val name: String
)
