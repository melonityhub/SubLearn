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

include(":app")

// Pure JVM core (fast unit tests, no Android dependency).
include(":core:model")
include(":core:subtitle")
include(":core:settings")
include(":core:later")
include(":core:ai")

// Android core: design tokens, Room/DataStore/Keystore, Media3 player, ML Kit translation.
include(":core:design")
include(":core:data")
include(":core:player")
include(":core:translation")

// Feature modules (Compose UI).
include(":feature:home")
include(":feature:words")
include(":feature:player")
include(":feature:settings")
