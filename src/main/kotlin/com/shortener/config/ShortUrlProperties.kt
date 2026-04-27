package com.shortener.config

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.net.URI
import java.net.URISyntaxException

/**
 * Type-safe configuration properties bound from application.properties.
 *
 * Why externalise these:
 * - baseUrl differs between dev (http://localhost:8080) and prod (e.g. https://short.example.com)
 * - codeLength can be tuned without code changes (longer = more capacity, shorter = better UX)
 *
 * @Validated triggers validation on the fields below at startup, so an
 * invalid config fails fast rather than at first request.
 */
@Validated
@ConfigurationProperties(prefix = "app.short-url")
data class ShortUrlProperties(

    @field:NotBlank
    val baseUrl: String,

    @field:Min(4)
    val codeLength: Int = 6
) {
    init {
        val uri = try {
            URI(baseUrl)
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException("app.short-url.base-url is not a valid URI: ${e.message}")
        }
        require(uri.scheme in setOf("http", "https")) {
            "app.short-url.base-url must use http or https, got: ${uri.scheme}"
        }
        require(!uri.host.isNullOrBlank()) {
            "app.short-url.base-url must have a valid host"
        }
    }
}
