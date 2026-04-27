package com.shortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Application entry point.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class UrlShortenerApplication

fun main(args: Array<String>) {
    runApplication<UrlShortenerApplication>(*args)
}
