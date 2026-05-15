package org.raterr.system

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.http.HttpStatus
import java.util.NoSuchElementException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `NoSuchElementException returns 404`() {
        val response = handler.handleNotFound(NoSuchElementException("Not found"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Not found", response.body?.message)
    }

    @Test
    fun `IllegalArgumentException returns 400`() {
        val response = handler.handleBadRequest(IllegalArgumentException("Bad request"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Bad request", response.body?.message)
    }

    @Test
    fun `generic Exception returns 500`() {
        val response = handler.handleGeneric(Exception("Server error"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Server error", response.body?.message)
    }
}
