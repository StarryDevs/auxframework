import com.palantir.gradle.gitversion.VersionDetails
import org.gradle.kotlin.dsl.invoke
import java.util.Date

plugins {
    id("build.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.git.version)

    `maven-publish`
}

group = "starry.auxframework.context"

dependencies {
    api(libs.bundles.kotlinx.ecosystem)
    api(libs.bundles.logback)
    api(libs.adventure.parser)

    testImplementation(kotlin("test"))
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}

val versionDetails: groovy.lang.Closure<VersionDetails> by extra

tasks.processResources {
    val resourceTargets = listOf("starry/auxframework/metadata.properties")
    val replaceProperties = mapOf(
        "version" to version,
        "versionDetails" to versionDetails(),
        "date" to Date(),
    )
    filesMatching(resourceTargets) {
        expand(replaceProperties)
    }
}
