plugins {
    id("build.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.plugin.serialization)

    `maven-publish`
    application
}

dependencies {
    api(project(":aux-context"))
    api(project(":aux-aop"))
    api(libs.bundles.adventure)

    testImplementation(kotlin("test"))
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}

application {
    mainClass.set("starry.auxframework.application.AuxApplicationKt")
}
