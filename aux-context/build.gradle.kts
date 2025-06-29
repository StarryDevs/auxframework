plugins {
    id("build.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)

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
