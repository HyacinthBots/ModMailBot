import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.github.jakemarsden.git-hooks")
    id("com.github.johnrengelman.shadow")
    id("io.gitlab.arturbosch.detekt")
    id("org.cadixdev.licenser")
}

group = "io.github.nocomment1105.modmailbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(libs.kord.extensions)
    implementation(libs.kord.extensions.unsafe)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kordx.emoji)

    // Logging dependencies
    implementation(libs.logback)
    implementation(libs.logging)

    // Formatting
    detektPlugins(libs.detekt)

    // Database
    implementation(libs.kmongo)
}

application {
    mainClass.set("io.github.nocomment1105.modmailbot.ModMailBot.kt")
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt updateLicenses")
    )
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            languageVersion = "1.7"
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
            incremental = true
        }
    }
    jar {
        manifest {
            attributes(
                "Main-Class" to "io.github.nocomment1105.modmailbot.ModMailBot.kt"
            )
        }
    }

    wrapper {
        gradleVersion = "7.5.1"
        distributionType = Wrapper.DistributionType.BIN
    }
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/detekt.yml")

    autoCorrect = true
}

license {
    setHeader(rootProject.file("HEADER"))
    include("**/*.kt")
}
