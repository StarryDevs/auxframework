import java.util.*

plugins {
    id("build.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)

    `maven-publish`
}

dependencies {
    api(libs.bundles.kotlinx.ecosystem)
    api(libs.bundles.logback)
    api(libs.adventure.parser)
    api(libs.bytebuddy)

    testImplementation(kotlin("test"))
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}

tasks.processResources {
    val resourceTargets = listOf("starry/auxframework/metadata.properties")
    val replaceProperties = mapOf(
        "version" to version,
        "date" to Date(),
    )
    filesMatching(resourceTargets) {
        expand(replaceProperties)
    }
}
