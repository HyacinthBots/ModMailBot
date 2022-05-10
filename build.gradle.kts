import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.github.jakemarsden.git-hooks")
    id("com.github.johnrengelman.shadow")
    id("io.gitlab.arturbosch.detekt")
}

group = "io.github.nocomment1105.modmailbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

dependencies {
    implementation(libs.kord.extensions)
    implementation(libs.kotlin.stdlib)

    // Logging dependencies
    implementation(libs.groovy)
    implementation(libs.logback)
    implementation(libs.logging)

    // Formatting
    detektPlugins(libs.detekt)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.sqlite.jdbc)
    implementation(libs.hikari)
}

application {
    // This is deprecated, but the Shadow plugin requires it
    @Suppress("DEPRECATION")
    mainClassName = "io.github.nocomment1105.modmailbot.ModMailBot.kt"
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

tasks.withType<KotlinCompile>().forEach {
    it.kotlinOptions.jvmTarget = "17"

    it.kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    it.incremental = true
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "io.github.nocomment1105.modmailbot.ModMailBot.kt"
        )
    }
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/detekt.yml")

    autoCorrect = true
}
