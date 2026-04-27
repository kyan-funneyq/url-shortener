package com.shortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.shortener.dto.ShortenRequest
import com.shortener.repository.ShortUrlStore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Full integration test using @SpringBootTest.
 *
 * Tests the entire stack: controller -> service -> in-memory store.
 * This catches integration issues that pure unit tests miss (Spring config,
 * JSON serialisation, validation triggering, exception handling, bean wiring).
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var store: ShortUrlStore

    @BeforeEach
    fun cleanStore() {
        store.clear()
    }

    @Test
    fun `POST shorten should return 201 with short URL details`() {
        val request = ShortenRequest("https://www.originenergy.com.au/electricity-gas/plans.html")

        mockMvc.perform(
            post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.shortCode").exists())
            .andExpect(jsonPath("$.shortUrl").exists())
            .andExpect(jsonPath("$.originalUrl").value(request.url))
            .andExpect(jsonPath("$.createdAt").exists())
    }

    @Test
    fun `POST shorten should return 400 for invalid URL`() {
        val request = ShortenRequest("not-a-real-url")

        mockMvc.perform(
            post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `POST shorten should return 400 for blank URL`() {
        mockMvc.perform(
            post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": ""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors.url").exists())
    }

    @Test
    fun `POST shorten should return 400 for non-http scheme`() {
        val request = ShortenRequest("ftp://example.com/file")

        mockMvc.perform(
            post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET short code should redirect to original URL with 302`() {
        // First, shorten a URL
        val originalUrl = "https://www.example.com/article/123"
        val shortenResponse = mockMvc.perform(
            post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ShortenRequest(originalUrl)))
        ).andReturn().response.contentAsString

        val shortCode = objectMapper.readTree(shortenResponse).get("shortCode").asText()

        // Now visit the short URL — should redirect
        mockMvc.perform(get("/$shortCode"))
            .andExpect(status().isFound) // 302
            .andExpect(header().string("Location", originalUrl))
    }

    @Test
    fun `GET short code should return 404 when not found`() {
        mockMvc.perform(get("/nonexistent"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `GET urls info should return URL details with click count`() {
        // Shorten and visit twice
        val response = mockMvc.perform(
            post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ShortenRequest("https://example.com")))
        ).andReturn().response.contentAsString

        val shortCode = objectMapper.readTree(response).get("shortCode").asText()

        // Visit twice to bump click count
        mockMvc.perform(get("/$shortCode"))
        mockMvc.perform(get("/$shortCode"))

        // Get info — should show click count of 2
        mockMvc.perform(get("/api/urls/$shortCode"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
            .andExpect(jsonPath("$.clickCount").value(2))
    }

    @Test
    fun `GET urls info should return 404 for unknown short code`() {
        mockMvc.perform(get("/api/urls/unknown"))
            .andExpect(status().isNotFound)
    }
}
