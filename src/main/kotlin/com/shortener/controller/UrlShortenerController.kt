package com.shortener.controller

import com.shortener.dto.ShortenRequest
import com.shortener.dto.ShortenResponse
import com.shortener.dto.UrlInfoResponse
import com.shortener.service.UrlShortenerService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST endpoints for shortening URLs and retrieving info.
 *
 * Endpoints:
 *   POST /api/shorten           -> create a short URL
 *   GET  /api/urls/{shortCode}  -> get info about a short URL (does not increment clicks)
 */
@RestController
@RequestMapping("/api")
class UrlShortenerController(
    private val urlShortenerService: UrlShortenerService
) {

    /**
     * Shortens a URL.
     *
     * Returns 201 Created (correct REST convention for resource creation)
     * with the new short URL details in the body.
     */
    @PostMapping("/shorten")
    fun shorten(@Valid @RequestBody request: ShortenRequest): ResponseEntity<ShortenResponse> {
        val saved = urlShortenerService.shorten(request.url)
        val response = ShortenResponse.from(saved, urlShortenerService.baseUrl())
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Returns info about a short URL — original URL, creation time, click count.
     * Useful for users who want to inspect a short URL without visiting it.
     */
    @GetMapping("/urls/{shortCode}")
    fun getInfo(@PathVariable shortCode: String): UrlInfoResponse {
        val entity = urlShortenerService.getInfo(shortCode)
        return UrlInfoResponse.from(entity, urlShortenerService.baseUrl())
    }
}
