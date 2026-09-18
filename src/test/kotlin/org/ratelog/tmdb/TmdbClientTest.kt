package org.ratelog.tmdb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.config.ConfigKey
import org.ratelog.config.GeneralConfig
import org.ratelog.test.InMemoryGeneralConfigRepository

class TmdbClientTest {

    private lateinit var repository: InMemoryGeneralConfigRepository
    private lateinit var client: TmdbClient

    @BeforeEach
    fun setUp() {
        repository = InMemoryGeneralConfigRepository()
        client = TmdbClient(repository, baseUrl = "http://localhost:0")
    }

    @Test
    fun `given no api key configured when searching then returns ApiKeyMissing error`() {
        // When
        val result = client.searchMovies(query = "matrix", lang = Lang.en)

        // Then
        assertTrue(result.isLeft())
        assertEquals(TmdbError.ApiKeyMissing, result.fold({ it }, { null }))
    }

    @Test
    fun `given a blank api key configured when searching then returns ApiKeyMissing error`() {
        // Given
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = "   ",
                updatedAtEpochMs = 0L,
            )
        )

        // When
        val result = client.searchMovies(query = "matrix", lang = Lang.en)

        // Then
        assertTrue(result.isLeft())
        assertEquals(TmdbError.ApiKeyMissing, result.fold({ it }, { null }))
    }

    @Test
    fun `given a blank api key configured when fetching movie details then returns ApiKeyMissing error`() {
        // Given
        repository.save(
            GeneralConfig(
                id = null,
                key = ConfigKey.TMDB_API_KEY,
                value = "",
                updatedAtEpochMs = 0L,
            )
        )

        // When
        val result = client.movieDetails(TmdbId(603))

        // Then
        assertTrue(result.isLeft())
        assertEquals(TmdbError.ApiKeyMissing, result.fold({ it }, { null }))
    }

    @Test
    fun `given a blank query when searching then returns empty result without needing the api key`() {
        // When
        val result = client.searchMovies(query = "", lang = Lang.en)

        // Then
        assertTrue(result.isRight())
        val (items, totalPages) = result.fold({ null to null }, { it })
        assertEquals(emptyList<TmdbMovieResponse>(), items)
        assertEquals(1, totalPages)
    }
}
