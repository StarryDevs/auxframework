plugins {
    id("build.convention.kotlin-jvm")

    `maven-publish`
}

dependencies {
    api(libs.bundles.adventure)
    api(project(":aux-context"))

    testImplementation(kotlin("test"))
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}
