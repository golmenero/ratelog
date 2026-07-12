package org.ratelog.tmdb

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieDescription
import org.ratelog.tvshow.TvDescription
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

    fun searchMovies(query: String, lang: Lang): Either<TmdbError, List<TmdbMovieResponse>> {
        if (query.isBlank()) return emptyList<TmdbMovieResponse>().right()
        requireApiKey()
        rateLimiter.acquire()

        val results = restClient.get()
            .uri { builder ->
                builder.path("/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", lang)
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .body(TmdbSearchResponse::class.java)
            ?.results
            ?: emptyList()

        return results.right()
    }

    fun movieDetails(tmdbId: TmdbId): Either<TmdbError, Movie> {
        requireApiKey()
        rateLimiter.acquire()

        return restClient.get()
            .uri { builder ->
                builder.path("/movie/{id}")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", Lang.en)
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbMovieResponse::class.java)
            ?.toDomain()
            ?.right()
            ?: TmdbError.MovieNotFound.left()
    }

    fun searchTvShows(query: String, lang: Lang): Either<TmdbError, List<TmdbTvShowResponse>> {
        if (query.isBlank()) return emptyList<TmdbTvShowResponse>().right()
        requireApiKey()
        rateLimiter.acquire()

        val results = restClient.get()
            .uri { builder ->
                builder.path("/search/tv")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", lang)
                    .queryParam("include_adult", false)
                    .queryParam("page", 1)
                    .queryParam("query", query)
                    .build()
            }
            .retrieve()
            .body(TmdbTvShowSearchResponse::class.java)
            ?.results
            ?: emptyList()

        return results.right()
    }

    fun tvShowDetails(tmdbId: TmdbId): Either<TmdbError, TvShow> {
        requireApiKey()
        rateLimiter.acquire()

        return restClient.get()
            .uri { builder ->
                builder.path("/tv/{id}")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", Lang.en)
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbTvShowResponse::class.java)
            ?.toDomain()
            ?.right()
            ?: TmdbError.TvShowNotFound.left()
    }

    fun movieTranslations(tmdbId: TmdbId): Either<TmdbError, List<MovieDescription>> {
        requireApiKey()
        rateLimiter.acquire()

        val allowedLanguages = Lang.entries.map { it.name }

        val response = restClient.get()
            .uri { builder ->
                builder.path("/movie/{id}/translations")
                    .queryParam("api_key", apiKey)
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbTranslationsResponse::class.java)

        val descriptions = response?.translations
            ?.mapNotNull { entry ->
                if (!allowedLanguages.contains(entry.iso6391)) return@mapNotNull null
                val title = entry.data.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

                MovieDescription(
                    id = null,
                    tmdbId = tmdbId,
                    lang = Lang.parse(entry.iso6391),
                    title = Title(title),
                    overview = entry.data.overview?.takeIf { it.isNotBlank() }?.let { Overview(it) },
                )
            }
            ?.groupBy { it.lang }
            ?.map { it.value.first() }
            ?: emptyList()

        return descriptions.right()
    }

    fun tvTranslations(tmdbId: TmdbId): Either<TmdbError, List<TvDescription>> {
        requireApiKey()
        rateLimiter.acquire()

        val allowedLanguages = Lang.entries.map { it.name }

        val response = restClient.get()
            .uri { builder ->
                builder.path("/tv/{id}/translations")
                    .queryParam("api_key", apiKey)
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbTranslationsResponse::class.java)

        val descriptions = response?.translations
            ?.mapNotNull { entry ->
                if (!allowedLanguages.contains(entry.iso6391)) return@mapNotNull null
                val name = entry.data.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null

                TvDescription(
                    id = null,
                    tmdbId = tmdbId,
                    lang = Lang.parse(entry.iso6391),
                    name = Title(name),
                    overview = entry.data.overview?.takeIf { it.isNotBlank() }?.let { Overview(it) },
                )
            }
            ?.groupBy { it.lang }
            ?.map { it.value.first() }
            ?: emptyList()

        return descriptions.right()
    }

    private fun requireApiKey() {
        require(apiKey.isNotBlank()) {
            "TMDB_API_KEY is missing. Set the environment variable before using search or details."
        }
    }
}
