package com.shortener.service

import com.shortener.config.ShortUrlProperties
import org.springframework.stereotype.Component
import java.security.SecureRandom

/**
 * Generates random short codes for URLs.
 *
 * Design choices:
 * - SecureRandom (not Random) — non-sequential, unpredictable codes (security best practice)
 * - Base62 alphabet (a-z, A-Z, 0-9) — URL-safe, dense (62^6 = 56 billion combinations for length 6)
 * - Default length 6 — short enough for users to type, large enough to avoid collisions for
 *   any realistic dataset
 *
 * Collision handling and retry policy live in UrlShortenerService, which owns the
 * "generate until stored" loop via saveIfAbsent.
 */
@Component
class ShortCodeGenerator(private val properties: ShortUrlProperties) {

    companion object {
        private const val ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    private val random = SecureRandom()

    fun generate(): String = (1..properties.codeLength)
        .map { ALPHABET[random.nextInt(ALPHABET.length)] }
        .joinToString("")
}
