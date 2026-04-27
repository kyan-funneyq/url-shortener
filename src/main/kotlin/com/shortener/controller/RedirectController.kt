package com.shortener.controller

import com.shortener.service.UrlShortenerService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class RedirectController(
    private val urlShortenerService: UrlShortenerService
) {

    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    fun redirect(@PathVariable shortCode: String): ResponseEntity<Void> {
        val entity = urlShortenerService.resolve(shortCode)

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, entity.originalUrl)
            .build()
    }
}
