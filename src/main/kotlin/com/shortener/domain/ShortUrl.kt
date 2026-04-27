package com.shortener.domain

import java.time.LocalDateTime

data class ShortUrl(
    val shortCode: String,
    val originalUrl: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val clickCount: Long = 0
) {
    fun recordClick(): ShortUrl = copy(clickCount = clickCount + 1)
}
