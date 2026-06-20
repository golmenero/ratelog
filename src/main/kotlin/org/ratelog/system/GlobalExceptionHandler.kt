package org.ratelog.system

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.NoSuchElementException

data class ApiError(val message: String)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(e.message ?: "Resource not found"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(e.message ?: "Invalid request"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errorMessage = e.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "Validation error"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(errorMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<ApiError> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(e.message ?: "Internal server error"))
    }
}
