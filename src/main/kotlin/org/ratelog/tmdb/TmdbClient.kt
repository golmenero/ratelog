package org.ratelog.tmdb

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import org.ratelog.Lang
import org.ratelog.Overview
import org.ratelog.Title
import org.ratelog.TmdbId
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfigRepository
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
    private val generalConfigRepository: GeneralConfigRepository,
    @Value("\${ratelog.tmdb.base-url}") private val baseUrl: String = "https://api.themoviedb.org/3",
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

    fun searchMovies(query: String, lang: Lang, page: Int = 1): Either<TmdbError, Pair<List<TmdbMovieResponse>, Int>> {
        if (query.isBlank()) return (emptyList<TmdbMovieResponse>() to 1).right()
        return either {
            requireApiKey().bind()
            rateLimiter.acquire()

            val response = restClient.get()
                .uri { builder ->
                    builder.path("/search/movie")
                        .queryParam("api_key", apiKey())
                        .queryParam("language", lang)
                        .queryParam("include_adult", false)
                        .queryParam("page", page)
                        .queryParam("query", query)
                        .build()
                }
                .retrieve()
                .body(TmdbSearchResponse::class.java)

            (response?.results ?: emptyList<TmdbMovieResponse>()) to (response?.totalPages ?: 1)
        }
    }

    fun movieDetails(tmdbId: TmdbId): Either<TmdbError, Movie> = either {
        requireApiKey().bind()
        rateLimiter.acquire()

        val response = restClient.get()
            .uri { builder ->
                builder.path("/movie/{id}")
                    .queryParam("api_key", apiKey())
                    .queryParam("language", Lang.en)
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbMovieResponse::class.java)

        ensure(response != null) { TmdbError.MovieNotFound }
        response.toDomain()
    }

    fun searchTvShows(query: String, lang: Lang, page: Int = 1): Either<TmdbError, Pair<List<TmdbTvShowResponse>, Int>> {
        if (query.isBlank()) return (emptyList<TmdbTvShowResponse>() to 1).right()
        return either {
            requireApiKey().bind()
            rateLimiter.acquire()

            val response = restClient.get()
                .uri { builder ->
                    builder.path("/search/tv")
                        .queryParam("api_key", apiKey())
                        .queryParam("language", lang)
                        .queryParam("include_adult", false)
                        .queryParam("page", page)
                        .queryParam("query", query)
                        .build()
                }
                .retrieve()
                .body(TmdbTvShowSearchResponse::class.java)

            (response?.results ?: emptyList<TmdbTvShowResponse>()) to (response?.totalPages ?: 1)
        }
    }

    fun trendingMovies(lang: Lang): Either<TmdbError, List<TmdbMovieResponse>> = either {
        requireApiKey().bind()
        rateLimiter.acquire()

        val response = restClient.get()
            .uri { builder ->
                builder.path("/trending/movie/week")
                    .queryParam("api_key", apiKey())
                    .queryParam("language", lang)
                    .build()
            }
            .retrieve()
            .body(TmdbSearchResponse::class.java)

        response?.results ?: emptyList()
    }

    fun trendingTvShows(lang: Lang): Either<TmdbError, List<TmdbTvShowResponse>> = either {
        requireApiKey().bind()
        rateLimiter.acquire()

        val response = restClient.get()
            .uri { builder ->
                builder.path("/trending/tv/week")
                    .queryParam("api_key", apiKey())
                    .queryParam("language", lang)
                    .build()
            }
            .retrieve()
            .body(TmdbTvShowSearchResponse::class.java)

        response?.results ?: emptyList()
    }

    fun tvShowDetails(tmdbId: TmdbId): Either<TmdbError, TvShow> = either {
        requireApiKey().bind()
        rateLimiter.acquire()

        val response = restClient.get()
            .uri { builder ->
                builder.path("/tv/{id}")
                    .queryParam("api_key", apiKey())
                    .queryParam("language", Lang.en)
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbTvShowResponse::class.java)

        ensure(response != null) { TmdbError.TvShowNotFound }
        response.toDomain()
    }

    fun movieTranslations(tmdbId: TmdbId, originalTitle: Title): Either<TmdbError, List<MovieDescription>> = either {
        requireApiKey().bind()
        rateLimiter.acquire()

        val allowedLanguages = Lang.entries.map { it.name }

        val response = restClient.get()
            .uri { builder ->
                builder.path("/movie/{id}/translations")
                    .queryParam("api_key", apiKey())
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbTranslationsResponse::class.java)

        response?.translations
            ?.mapNotNull { entry ->
                if (!allowedLanguages.contains(entry.iso6391)) return@mapNotNull null
                val title = entry.data.title?.takeIf { it.isNotBlank() }?.let(::Title)
                    ?: originalTitle

                MovieDescription(
                    id = null,
                    tmdbId = tmdbId,
                    lang = Lang.parse(entry.iso6391),
                    title = title,
                    overview = entry.data.overview?.takeIf { it.isNotBlank() }?.let { Overview(it) },
                )
            }
            ?.groupBy { it.lang }
            ?.map { it.value.first() }
            ?: emptyList()
    }

    fun tvTranslations(tmdbId: TmdbId, originalTitle: Title): Either<TmdbError, List<TvDescription>> = either {
        requireApiKey().bind()
        rateLimiter.acquire()

        val allowedLanguages = Lang.entries.map { it.name }

        val response = restClient.get()
            .uri { builder ->
                builder.path("/tv/{id}/translations")
                    .queryParam("api_key", apiKey())
                    .build(tmdbId.value)
            }
            .retrieve()
            .body(TmdbTranslationsResponse::class.java)

        response?.translations
            ?.mapNotNull { entry ->
                if (!allowedLanguages.contains(entry.iso6391)) return@mapNotNull null
                val name = entry.data.name?.takeIf { it.isNotBlank() }?.let(::Title)
                    ?: originalTitle

                TvDescription(
                    id = null,
                    tmdbId = tmdbId,
                    lang = Lang.parse(entry.iso6391),
                    name = name,
                    overview = entry.data.overview?.takeIf { it.isNotBlank() }?.let { Overview(it) },
                )
            }
            ?.groupBy { it.lang }
            ?.map { it.value.first() }
            ?: emptyList()
    }

    private fun apiKey(): String =
        generalConfigRepository.findByKey(ConfigKey.TMDB_API_KEY)?.value.orEmpty()

    private fun requireApiKey(): Either<TmdbError, Unit> {
        if (apiKey().isBlank()) {
            return TmdbError.ApiKeyMissing.left()
        }
        return Unit.right()
    }
}
