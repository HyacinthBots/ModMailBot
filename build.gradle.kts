
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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

group = "org.hyacinthbots.modmailbot"
version = "1.0-SNAPSHOT"
val javaTarget = 17

repositories {
    mavenCentral()

    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
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
    mainClass.set("org.hyacinthbots.modmailbot.ModMailBot.kt")
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt updateLicenses")
    )
}

kotlin {
    jvmToolchain(javaTarget)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaTarget.toString()))
            languageVersion.set(KotlinVersion.fromVersion(libs.versions.kotlin.get().substringBeforeLast(".")))
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            incremental = true
        }
    }
    jar {
        manifest {
            attributes(
                "Main-Class" to "org.hyacinthbots.modmailbot.ModMailBot.kt"
            )
        }
    }

    java {
        sourceCompatibility = JavaVersion.toVersion(javaTarget)
        targetCompatibility = JavaVersion.toVersion(javaTarget)
    }

    wrapper {
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
