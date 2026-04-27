package com.shortener.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request DTO for shortening a URL.
 *
 * Validation rules:
 * - @NotBlank: rejects null, empty, and whitespace-only values
 * - @Size: prevents abuse with absurdly long URLs (2048 matches browser/RFC limits)
 */
data class ShortenRequest(
    @field:NotBlank(message = "URL is required")
    @field:Size(max = 2048, message = "URL must not exceed 2048 characters")
    val url: String
)
