plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"     // opens classes for Spring's CGLIB proxies
}

group = "com.shortener"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web for REST endpoints
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Bean Validation for @Valid, @NotBlank, etc.
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin essentials
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Jackson Kotlin module for proper data class serialisation
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Mockito-Kotlin for cleaner mocking syntax
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"   // strict null safety for Java interop
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
