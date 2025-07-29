plugins {
    id("build.convention.kotlin-jvm")

    `maven-publish`
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}
