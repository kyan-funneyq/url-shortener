# URL Shortener API

A Spring Boot REST API that shortens long URLs and redirects short codes back to their original destinations.

Built with **Kotlin + Spring Boot 3.2 + ConcurrentHashMap (in-memory) + JUnit 5 + Mockito**.

---

## Prerequisites

- **JDK 17 or newer** (the build targets Java 17)
- **Gradle wrapper** (included in this repo — `./gradlew` on Mac/Linux, `gradlew.bat` on Windows)

Verify:
```bash
# Mac/Linux
java --version     # 17+
./gradlew --version

# Windows
java --version
gradlew.bat --version
```

---

## Run Locally

**Mac/Linux:**
```bash
./gradlew bootRun
```

**Windows:**
```bat
gradlew.bat bootRun
```

The app starts on `http://localhost:8080`.

Note: generated `shortUrl` values use `app.short-url.base-url`, which defaults to `http://short.ly` in `src/main/resources/application.properties`. For local end-to-end testing, change it to `http://localhost:8080`.

To package as a runnable JAR:

**Mac/Linux:**
```bash
./gradlew bootJar
java -jar build/libs/url-shortener-0.0.1-SNAPSHOT.jar
```

**Windows:**
```bat
gradlew.bat bootJar
java -jar build/libs/url-shortener-0.0.1-SNAPSHOT.jar
```

---

## Run Tests

**Mac/Linux:**
```bash
./gradlew test
```

**Windows:**
```bat
gradlew.bat test
```

This runs both unit tests and the integration test (`@SpringBootTest`) which spins up the full Spring context using the in-memory store. Test reports are written to `build/reports/tests/test/index.html`.

---

## API Reference

### 1. Shorten a URL — `POST /api/shorten`

**Request (Mac/Linux):**
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.originenergy.com.au/electricity-gas/plans.html"}'
```

**Request (Windows CMD):**
```bat
curl -X POST http://localhost:8080/api/shorten -H "Content-Type: application/json" -d "{\"url\": \"https://www.originenergy.com.au/electricity-gas/plans.html\"}"
```

**Response (201 Created):**
```json
{
  "shortCode": "a1B2c3",
  "shortUrl": "http://short.ly/a1B2c3",
  "originalUrl": "https://www.originenergy.com.au/electricity-gas/plans.html",
  "createdAt": "2026-04-26T14:30:00"
}
```

**Error (400 Bad Request) — invalid URL:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "URL scheme must be http or https, got: ftp",
  "timestamp": "2026-04-26T14:30:00"
}
```

**Error (400 Bad Request) — blank URL:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "fieldErrors": {
    "url": "URL is required"
  },
  "timestamp": "2026-04-26T14:30:00"
}
```

### 2. Redirect — `GET /{shortCode}`

```bash
curl -i http://localhost:8080/a1B2c3
```

**Response (302 Found):**
```
HTTP/1.1 302
Location: https://www.originenergy.com.au/electricity-gas/plans.html
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Short URL not found: nonexistent",
  "timestamp": "2026-04-26T14:30:00"
}
```

### 3. Get URL Info — `GET /api/urls/{shortCode}`

```bash
curl http://localhost:8080/api/urls/a1B2c3
```

**Response (200 OK):**
```json
{
  "shortCode": "a1B2c3",
  "shortUrl": "http://short.ly/a1B2c3",
  "originalUrl": "https://www.originenergy.com.au/electricity-gas/plans.html",
  "createdAt": "2026-04-26T14:30:00",
  "clickCount": 5
}
```

---

## Architecture

Standard layered Spring Boot architecture:

```
┌────────────────┐
│  Controller    │  REST endpoints, request/response DTOs, validation triggers
└───────┬────────┘
        ↓
┌────────────────┐
│  Service       │  Business logic
│   ↳ Validator  │  URL format validation
│   ↳ Generator  │  Short code generation
└───────┬────────┘
        ↓
┌────────────────┐
│  Store         │  ConcurrentHashMap-backed in-memory persistence
└────────────────┘
```

Cross-cutting:
- `GlobalExceptionHandler` — centralised exception → HTTP response mapping
- `ShortUrlProperties` — externalised configuration via `@ConfigurationProperties`

---

## Project Layout

```
src/main/kotlin/com/shortener/
├── UrlShortenerApplication.kt          # Spring Boot entry point
├── config/
│   └── ShortUrlProperties.kt           # @ConfigurationProperties
├── controller/
│   ├── UrlShortenerController.kt       # POST /api/shorten, GET /api/urls/{code}
│   └── RedirectController.kt           # GET /{code} -> 302 redirect
├── service/
│   ├── UrlShortenerService.kt          # Core business logic
│   ├── UrlValidator.kt                 # URL format validation
│   └── ShortCodeGenerator.kt           # Random base62 code generation
├── dto/
│   ├── ShortenRequest.kt               # Request DTO with @NotBlank, @Size
│   └── Responses.kt                    # ShortenResponse, UrlInfoResponse
├── domain/
│   └── ShortUrl.kt                     # In-memory domain model
├── repository/
│   └── ShortUrlStore.kt                # ConcurrentHashMap-backed store
└── exception/
    ├── Exceptions.kt                   # Domain exceptions
    └── GlobalExceptionHandler.kt       # @RestControllerAdvice

src/test/kotlin/com/shortener/
├── service/
│   ├── UrlValidatorTest.kt             # Parameterised unit tests
│   ├── ShortCodeGeneratorTest.kt       # Unit tests for random code generation
│   └── UrlShortenerServiceTest.kt      # Service logic with all collaborators mocked
└── controller/
    └── UrlShortenerIntegrationTest.kt  # @SpringBootTest end-to-end tests
```
