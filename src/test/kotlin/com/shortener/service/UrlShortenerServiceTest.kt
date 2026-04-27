package com.shortener.service

import com.shortener.config.ShortUrlProperties
import com.shortener.domain.ShortUrl
import com.shortener.exception.ShortCodeGenerationException
import com.shortener.exception.ShortUrlNotFoundException
import com.shortener.repository.ShortUrlStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UrlShortenerServiceTest {

    private val store: ShortUrlStore = mock()
    private val validator: UrlValidator = mock()
    private val codeGenerator: ShortCodeGenerator = mock()
    private val properties = ShortUrlProperties(baseUrl = "http://short.ly", codeLength = 6)

    private val service = UrlShortenerService(store, validator, codeGenerator, properties)

    @Test
    fun `shorten should validate url generate code and save`() {
        val originalUrl = "https://example.com/long/path"
        whenever(codeGenerator.generate()).thenReturn("abc123")
        whenever(store.saveIfAbsent(org.mockito.kotlin.any())).thenAnswer { it.arguments[0] }

        val result = service.shorten(originalUrl)

        assertEquals("abc123", result.shortCode)
        assertEquals(originalUrl, result.originalUrl)
        verify(validator).validate(originalUrl)
        verify(codeGenerator).generate()
        verify(store).saveIfAbsent(org.mockito.kotlin.any<ShortUrl>())
    }

    @Test
    fun `shorten should trim whitespace from url`() {
        whenever(codeGenerator.generate()).thenReturn("abc123")
        whenever(store.saveIfAbsent(org.mockito.kotlin.any())).thenAnswer { it.arguments[0] }

        val result = service.shorten("  https://example.com  ")

        assertEquals("https://example.com", result.originalUrl)
    }

    @Test
    fun `shorten should retry when saveIfAbsent returns null`() {
        whenever(codeGenerator.generate()).thenReturn("abc123")
        whenever(store.saveIfAbsent(org.mockito.kotlin.any()))
            .thenReturn(null)
            .thenAnswer { it.arguments[0] }

        val result = service.shorten("https://example.com")

        assertEquals("abc123", result.shortCode)
        verify(store, org.mockito.kotlin.times(2)).saveIfAbsent(org.mockito.kotlin.any())
    }

    @Test
    fun `shorten should throw when all saveIfAbsent attempts fail`() {
        whenever(codeGenerator.generate()).thenReturn("abc123")
        whenever(store.saveIfAbsent(org.mockito.kotlin.any())).thenReturn(null)

        assertThrows<ShortCodeGenerationException> { service.shorten("https://example.com") }
    }

    @Test
    fun `resolve should return url and increment click count`() {
        val entity = ShortUrl(shortCode = "abc123", originalUrl = "https://example.com")
        whenever(store.incrementClickCount("abc123")).thenReturn(entity.recordClick())
        val result = service.resolve("abc123")

        assertEquals("https://example.com", result.originalUrl)
        assertEquals(1, result.clickCount)
    }

    @Test
    fun `resolve should throw when short code not found`() {
        whenever(store.incrementClickCount("missing")).thenReturn(null)

        assertThrows<ShortUrlNotFoundException> { service.resolve("missing") }
    }

    @Test
    fun `getInfo should not increment click count`() {
        val entity = ShortUrl(shortCode = "abc123", originalUrl = "https://example.com", clickCount = 5)
        whenever(store.findByShortCode("abc123")).thenReturn(entity)

        val result = service.getInfo("abc123")

        assertEquals(5, result.clickCount)
    }

    @Test
    fun `getInfo should throw when short code not found`() {
        whenever(store.findByShortCode("missing")).thenReturn(null)

        assertThrows<ShortUrlNotFoundException> { service.getInfo("missing") }
    }
}
