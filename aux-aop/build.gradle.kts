plugins {
    id("build.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)

    `maven-publish`
}

dependencies {
    api(project(":aux-context"))
    api(libs.bytebuddy)

    testImplementation(kotlin("test"))
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}
