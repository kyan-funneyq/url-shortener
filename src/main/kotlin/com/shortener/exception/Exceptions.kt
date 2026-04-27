package com.shortener.exception

/**
 * Thrown when a short code does not match any stored URL.
 * Mapped to HTTP 404 by the global exception handler.
 */
class ShortUrlNotFoundException(shortCode: String)
    : RuntimeException("Short URL not found: $shortCode")

/**
 * Thrown when an incoming URL fails validation (malformed, unsupported scheme, etc.).
 * Mapped to HTTP 400 by the global exception handler.
 */
class InvalidUrlException(message: String) : RuntimeException(message)

/**
 * Thrown when short code generation cannot find a unique value after
 * the maximum number of retries. This is extremely unlikely under normal
 * load but signals exhausted code space — an operational concern (HTTP 500).
 */
class ShortCodeGenerationException(message: String) : RuntimeException(message)
