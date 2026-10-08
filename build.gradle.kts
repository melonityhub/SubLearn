// Root build. Plugin versions live in gradle/libs.versions.toml; modules apply them.
plugins {
    alias(libs.plugins.kotlin.serialization) apply false
}
