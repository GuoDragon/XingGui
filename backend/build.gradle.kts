import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
}

apply(plugin = "org.jetbrains.kotlin.jvm")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

application {
    mainClass.set("com.example.xinggui.backend.ApplicationKt")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)
    implementation(libs.hikari)
    implementation(libs.mysql.connector)
    implementation(libs.logback.classic)

    testImplementation(libs.junit)
}
