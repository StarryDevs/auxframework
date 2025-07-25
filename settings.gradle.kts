dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":aux-context")
include(":aux-aop")
include(":aux-web")
include(":aux-validation")
include(":aux-application")

rootProject.name = "auxframework"
