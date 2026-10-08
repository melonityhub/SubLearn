pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SubLearn"

// Pure JVM core (fast unit tests, no Android dependency). Android modules are added in the next phase commit.
include(":core:model")
include(":core:subtitle")
include(":core:settings")
include(":core:later")
include(":core:ai")

// Android core (Room, DataStore, Keystore, Media3, Compose design tokens).
include(":core:design")
include(":core:data")
include(":core:player")
