package com.shortener.repository

import com.shortener.domain.ShortUrl
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe in-memory store for shortened URLs.
 *
 * ConcurrentHashMap keeps the solution simple while remaining safe for
 * concurrent requests within a single application instance.
 */
@Repository
class ShortUrlStore {

    private val urlsByCode = ConcurrentHashMap<String, ShortUrl>()

    fun existsByShortCode(shortCode: String): Boolean = urlsByCode.containsKey(shortCode)

    fun saveIfAbsent(shortUrl: ShortUrl): ShortUrl? =
        if (urlsByCode.putIfAbsent(shortUrl.shortCode, shortUrl) == null) shortUrl else null

    fun findByShortCode(shortCode: String): ShortUrl? = urlsByCode[shortCode]

    fun incrementClickCount(shortCode: String): ShortUrl? =
        urlsByCode.computeIfPresent(shortCode) { _, existing -> existing.recordClick() }

    fun clear() {
        urlsByCode.clear()
    }
}
