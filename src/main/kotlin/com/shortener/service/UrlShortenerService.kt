package com.shortener.service

import com.shortener.config.ShortUrlProperties
import com.shortener.domain.ShortUrl
import com.shortener.exception.ShortCodeGenerationException
import com.shortener.exception.ShortUrlNotFoundException
import com.shortener.repository.ShortUrlStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Core service orchestrating URL shortening and resolution.
 *
 * Responsibilities:
 * - Validate incoming URLs (delegated to UrlValidator)
 * - Generate unique short codes (delegated to ShortCodeGenerator)
 * - Store URLs in memory
 * - Resolve short codes back to original URLs
 * - Track click counts on resolution
 */
@Service
class UrlShortenerService(
    private val store: ShortUrlStore,
    private val validator: UrlValidator,
    private val codeGenerator: ShortCodeGenerator,
    private val properties: ShortUrlProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_RETRIES = 5
    }

    fun shorten(originalUrl: String): ShortUrl {
        val trimmedUrl = originalUrl.trim()
        validator.validate(trimmedUrl)

        repeat(MAX_RETRIES) {
            val shortCode = codeGenerator.generate()
            val entity = ShortUrl(
                shortCode = shortCode,
                originalUrl = trimmedUrl
            )

            val saved = store.saveIfAbsent(entity)
            if (saved != null) {
                log.info("Shortened URL: {} -> {}", saved.originalUrl, saved.shortCode)
                return saved
            }
        }

        throw ShortCodeGenerationException("Failed to store unique short code after $MAX_RETRIES attempts")
    }

    /**
     * Resolves a short code to the original URL and increments click count.
     * Used by the redirect endpoint
     */
    fun resolve(shortCode: String): ShortUrl {
        return store.incrementClickCount(shortCode)
            ?: throw ShortUrlNotFoundException(shortCode)
    }

    /**
     * Returns metadata about a short URL without incrementing click count.
     * Used by the optional info endpoint where users want to inspect, not visit.
     */
    fun getInfo(shortCode: String): ShortUrl {
        return store.findByShortCode(shortCode)
            ?: throw ShortUrlNotFoundException(shortCode)
    }

    fun baseUrl(): String = properties.baseUrl.trimEnd('/')
}
