package org.ratelog.system

import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.NoSuchElementException

data class ApiError(val message: String)

@ControllerAdvice
class GlobalExceptionHandler(private val messageSource: MessageSource) {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private fun msg(key: String): String {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale())
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<ApiError> {
        logger.warn("Resource not found: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(msg("api.error.not.found")))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ApiError> {
        logger.warn("Bad request: ${e.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(msg("api.error.bad.request")))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        logger.warn("Validation error: ${e.bindingResult.fieldErrors.map { it.defaultMessage }}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(msg("api.error.validation.failed")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<ApiError> {
        logger.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(msg("api.error.internal")))
    }
}
