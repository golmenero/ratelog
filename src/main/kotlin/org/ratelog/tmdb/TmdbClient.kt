package org.ratelog.tmdb

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.movie.Movie
import org.ratelog.tvshow.TvShow
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Duration

@Component
class TmdbClient(
    @Value("\${ratelog.tmdb.api-key}") private val apiKey: String,
    @Value("\${ratelog.tmdb.base-url}") private val baseUrl: String = "https://api.themoviedb.org/3"
) {
    private val rateLimiter = TmdbRateLimiter(maxRequestsPerSecond = 40)

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(
            JdkClientHttpRequestFactory(
                java.net.http.HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build()
            ).apply {
                setReadTimeout(Duration.ofSeconds(10))
            }
        )
        .build()

    fun searchMovies(query: String): Either<TmdbError, List<Movie>> {
        if (query.isBlank()) return emptyList<Movie>().right()
        requireApiKey()
        rateLimiter.acquire()

        val results = restClient.get()
            .uri { builder ->
                builder.path("/search/movie")
                    .queryParam("language", Lang.en)
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .header("Authorization", "Bearer $apiKey")
            .retrieve()
            .body(TmdbSearchResponse::class.java)
            ?.results
            ?.map { it.toDomain() }
            ?: emptyList()

        return results.right()
    }

    fun movieDetails(tmdbId: TmdbId): Either<TmdbError, Movie> {
        requireApiKey()
        rateLimiter.acquire()

        return restClient.get()
            .uri { builder ->
                builder.path("/movie/{id}")
                    .queryParam("language", Lang.en)
                    .build(tmdbId.value)
            }
            .header("Authorization", "Bearer $apiKey")
            .retrieve()
            .body(TmdbMovieResponse::class.java)
            ?.toDomain()
            ?.right()
            ?: TmdbError.MovieNotFound.left()
    }

    fun searchTvShows(query: String): Either<TmdbError, List<TvShow>> {
        if (query.isBlank()) return emptyList<TvShow>().right()
        requireApiKey()
        rateLimiter.acquire()

        val results = restClient.get()
            .uri { builder ->
                builder.path("/search/tv")
                    .queryParam("language", Lang.en)
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .header("Authorization", "Bearer $apiKey")
            .retrieve()
            .body(TmdbTvShowSearchResponse::class.java)
            ?.results
            ?.map { it.toDomain() }
            ?: emptyList()

        return results.right()
    }

    fun tvShowDetails(tmdbId: TmdbId): Either<TmdbError, TvShow> {
        requireApiKey()
        rateLimiter.acquire()

        return restClient.get()
            .uri { builder ->
                builder.path("/tv/{id}")
                    .queryParam("language", Lang.en)
                    .build(tmdbId.value)
            }
            .header("Authorization", "Bearer $apiKey")
            .retrieve()
            .body(TmdbTvShowResponse::class.java)
            ?.toDomain()
            ?.right()
            ?: TmdbError.TvShowNotFound.left()
    }

    private fun requireApiKey() {
        require(apiKey.isNotBlank()) {
            "TMDB_API_KEY is missing. Set the environment variable before using search or details."
        }
    }
}
