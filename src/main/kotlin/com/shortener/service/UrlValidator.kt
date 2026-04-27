package com.shortener.service

import com.shortener.exception.InvalidUrlException
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URISyntaxException

/**
 * Validates incoming URLs before they're shortened.
 *
 * Rules:
 * - Must be a syntactically valid URI
 * - Must use http or https scheme (we don't shorten ftp://, javascript:, etc.)
 * - Must have a host
 */
@Component
class UrlValidator {

    private val allowedSchemes = setOf("http", "https")

    fun validate(url: String) {
        if (url.isEmpty()) {
            throw InvalidUrlException("URL must not be empty")
        }

        val uri = try {
            URI(url)
        } catch (e: URISyntaxException) {
            throw InvalidUrlException("URL is malformed: ${e.message}")
        }

        val scheme = uri.scheme?.lowercase()
            ?: throw InvalidUrlException("URL must include a scheme (http:// or https://)")

        if (scheme !in allowedSchemes) {
            throw InvalidUrlException("URL scheme must be http or https, got: $scheme")
        }

        if (uri.host.isNullOrBlank()) {
            throw InvalidUrlException("URL must include a host")
        }
    }
}
