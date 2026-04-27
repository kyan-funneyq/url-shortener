package com.shortener.service

import com.shortener.exception.InvalidUrlException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class UrlValidatorTest {

    private val validator = UrlValidator()

    @ParameterizedTest
    @ValueSource(strings = [
        "https://www.example.com",
        "http://example.com",
        "https://www.originenergy.com.au/electricity-gas/plans.html",
        "https://example.com/path?query=1&other=2",
        "http://localhost:8080/test"
    ])
    fun `should accept valid http and https URLs`(url: String) {
        validator.validate(url)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "ftp://example.com",
        "javascript:alert('xss')",
        "file:///etc/passwd"
    ])
    fun `should reject non-http schemes`(url: String) {
        assertThrows<InvalidUrlException> { validator.validate(url) }
    }

    @Test
    fun `should reject URL without scheme`() {
        assertThrows<InvalidUrlException> { validator.validate("www.example.com") }
    }

    @Test
    fun `should reject malformed URL`() {
        assertThrows<InvalidUrlException> { validator.validate("ht!tp://bad url") }
    }

    @Test
    fun `should reject empty URL`() {
        assertThrows<InvalidUrlException> { validator.validate("") }
    }

    @Test
    fun `should reject whitespace-only URL`() {
        assertThrows<InvalidUrlException> { validator.validate("   ") }
    }

    @Test
    fun `should reject URL without host`() {
        assertThrows<InvalidUrlException> { validator.validate("http://") }
    }
}
