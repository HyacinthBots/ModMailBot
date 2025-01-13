
import dev.kordex.gradle.plugins.kordex.DataCollection
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.detekt)
    alias(libs.plugins.git.hooks)
    alias(libs.plugins.kord.extensions.plugin)
    alias(libs.plugins.licenser)
}

group = "org.hyacinthbots.modmailbot"
version = "1.0-SNAPSHOT"

val className = "org.hyacinthbots.modmailbot.ModMailBotKt"
val javaTarget = "21"

repositories {
    mavenCentral()

    maven {
        name = "Kord Snapshots"
        url = uri("https://repo.kord.dev/snapshots")
    }

    maven {
        name = "Kord Extensions (Releases)"
        url = uri("https://releases-repo.kordex.dev")
    }

    maven {
        name = "Kord Extensions (Snapshots)"
        url = uri("https://snapshots-repo.kordex.dev")
    }

    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation(libs.kord.extensions)
    implementation(libs.kord.extensions.unsafe)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kordx.emoji)

    // Logging dependencies
    implementation(libs.logback)
    implementation(libs.logback.groovy)
    implementation(libs.logging)
    implementation(libs.groovy)
    implementation(libs.jansi)

    // Formatting
    detektPlugins(libs.detekt)

    // Database
    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)
}

kordEx {
    addDependencies = false
    addRepositories = false
    ignoreIncompatibleKotlinVersion = true

    bot {
        dataCollection(DataCollection.None)
    }

    i18n {
        classPackage = "modmailbot.i18n"
        translationBundle = "modmailbot.strings"
    }
}

application {
    mainClass.set(className)
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt updateLicenses")
    )
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaTarget))
            languageVersion.set(KotlinVersion.fromVersion(libs.versions.kotlin.get().substringBeforeLast(".")))
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            incremental = true
        }
    }
    jar {
        manifest {
            attributes("Main-Class" to className)
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
    config.setFrom("$rootDir/detekt.yml")

    autoCorrect = true
}

license {
    setHeader(rootProject.file("HEADER"))
    include("**/*.kt")
}
