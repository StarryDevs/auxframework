plugins {
    id("build.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    api(libs.bundles.kotlinx.ecosystem)
    api(libs.bundles.logback)
    api(libs.adventure.parser)

    testImplementation(kotlin("test"))
}