package com.shortener.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: Map<String, String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ShortUrlNotFoundException::class)
    fun handleNotFound(e: ShortUrlNotFoundException): ResponseEntity<ApiError> {
        log.warn("Short URL not found: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiError(
                status = 404,
                error = "Not Found",
                message = e.message ?: "Resource not found"
            )
        )
    }

    @ExceptionHandler(InvalidUrlException::class)
    fun handleInvalidUrl(e: InvalidUrlException): ResponseEntity<ApiError> {
        log.warn("Invalid URL submitted: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = 400,
                error = "Bad Request",
                message = e.message ?: "Invalid URL"
            )
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMalformedJson(e: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        log.warn("Malformed JSON request: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = 400,
                error = "Bad Request",
                message = "Malformed JSON request"
            )
        )
    }

    /**
     * Bean Validation failures (@Valid) on request DTOs.
     * Returns a fieldErrors map so the client knows exactly which fields failed.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fieldErrors = e.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }

        log.warn("Validation failed: {}", fieldErrors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(
                status = 400,
                error = "Validation Failed",
                message = "Request validation failed",
                fieldErrors = fieldErrors
            )
        )
    }

    @ExceptionHandler(ShortCodeGenerationException::class)
    fun handleGenerationFailure(e: ShortCodeGenerationException): ResponseEntity<ApiError> {
        log.error("Short code generation failed", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError(
                status = 500,
                error = "Internal Server Error",
                message = "Unable to generate short code, please try again"
            )
        )
    }

    /**
     * Catch-all for unexpected exceptions.
     * Logs the full stack trace internally; returns a generic message to the client
     * to avoid leaking implementation details (class names, stack traces, etc.).
     */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiError> {
        log.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError(
                status = 500,
                error = "Internal Server Error",
                message = "Something went wrong"
            )
        )
    }
}
