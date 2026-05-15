package org.raterr.tmdb

import arrow.core.Either
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TmdbClientTest {

    @Test
    fun `searchMovies with blank query returns empty list`() {
        val client = TmdbClient("test-key", "http://localhost:0")
        val result = client.searchMovies("")

        assertTrue(result.isRight())
        assertEquals(0, (result as Either.Right).value.size)
    }

    @Test
    fun `searchTvShows with blank query returns empty list`() {
        val client = TmdbClient("test-key", "http://localhost:0")
        val result = client.searchTvShows("   ")

        assertTrue(result.isRight())
        assertEquals(0, (result as Either.Right).value.size)
    }

    @Test
    fun `movieDetails with missing API key throws IllegalArgumentException`() {
        val client = TmdbClient("", "http://localhost:0")

        assertThrows<IllegalArgumentException> {
            client.movieDetails(123)
        }
    }

    @Test
    fun `tvShowDetails with missing API key throws IllegalArgumentException`() {
        val client = TmdbClient("", "http://localhost:0")

        assertThrows<IllegalArgumentException> {
            client.tvShowDetails(456)
        }
    }
}
