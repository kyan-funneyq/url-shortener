package com.shortener.service

import com.shortener.config.ShortUrlProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShortCodeGeneratorTest {

    private val properties = ShortUrlProperties(baseUrl = "http://short.ly", codeLength = 6)
    private val generator = ShortCodeGenerator(properties)

    @Test
    fun `should generate code of configured length`() {
        assertEquals(6, generator.generate().length)
    }

    @Test
    fun `should generate alphanumeric code`() {
        assertTrue(generator.generate().matches(Regex("[a-zA-Z0-9]+")))
    }

    @Test
    fun `should generate different codes on consecutive calls`() {
        val codes = (1..100).map { generator.generate() }.toSet()

        assertTrue(codes.size > 95, "Expected near-100 unique codes, got ${codes.size}")
    }
}
