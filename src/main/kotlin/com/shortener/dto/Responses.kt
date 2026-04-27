package com.shortener.dto

import com.shortener.domain.ShortUrl
import java.time.LocalDateTime

/**
 * Response returned when a URL is shortened.
 */
data class ShortenResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: LocalDateTime
) {
    companion object {
        /**
         * Factory method to build a response from the domain model.
         * Keeping the conversion logic here (rather than in the controller)
         * keeps controllers thin and the conversion testable in isolation.
         */
        fun from(shortUrl: ShortUrl, baseUrl: String) = ShortenResponse(
            shortCode = shortUrl.shortCode,
            shortUrl = "$baseUrl/${shortUrl.shortCode}",
            originalUrl = shortUrl.originalUrl,
            createdAt = shortUrl.createdAt
        )
    }
}

/**
 * Response for the optional URL-info endpoint (GET /api/urls/{code}).
 * Includes click stats which are useful info but not returned on every redirect.
 */
data class UrlInfoResponse(
    val shortCode: String,
    val shortUrl: String,
    val originalUrl: String,
    val createdAt: LocalDateTime,
    val clickCount: Long
) {
    companion object {
        fun from(shortUrl: ShortUrl, baseUrl: String) = UrlInfoResponse(
            shortCode = shortUrl.shortCode,
            shortUrl = "$baseUrl/${shortUrl.shortCode}",
            originalUrl = shortUrl.originalUrl,
            createdAt = shortUrl.createdAt,
            clickCount = shortUrl.clickCount
        )
    }
}
